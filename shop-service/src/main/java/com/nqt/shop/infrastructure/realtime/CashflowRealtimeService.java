package com.nqt.shop.infrastructure.realtime;

import com.nqt.shop.usecase.dto.CashflowRecordResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class CashflowRealtimeService {

    // Emitter timeout: 30 phút (1.800.000 ms)
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Đăng ký một Admin client lắng nghe luồng sự kiện thanh toán realtime
     */
    public SseEmitter registerClient() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitter.onCompletion(() -> {
            log.info("SSE Client đã ngắt kết nối bình thường. Active emitters: {}", emitters.size() - 1);
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE Client timeout. Active emitters: {}", emitters.size() - 1);
            emitter.complete();
            emitters.remove(emitter);
        });

        emitter.onError(throwable -> {
            log.warn("SSE Client gặp lỗi ({}). Active emitters: {}", throwable.getMessage(), emitters.size() - 1);
            emitter.complete();
            emitters.remove(emitter);
        });

        emitters.add(emitter);
        log.info("SSE Client mới kết nối thành công. Tổng clients kết nối: {}", emitters.size());

        // Gửi sự kiện chào mừng / kiểm tra đường truyền ban đầu
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to PENTAVA Cashflow Realtime Stream"));
        } catch (IOException e) {
            log.warn("Gửi sự kiện INIT thất bại: {}", e.getMessage());
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Bắn thông báo giao dịch dòng tiền realtime tới tất cả các Admin client đang mở trang
     */
    public void broadcastTransaction(CashflowRecordResponseDTO record) {
        if (emitters.isEmpty()) {
            log.debug("Không có Admin client nào đang mở SSE stream.");
            return;
        }

        log.info("Broadcasting realtime payment to {} clients: txCode={}, type={}, amountVnd={}",
                emitters.size(), record.getTransactionCode(), record.getType(), record.getAmountVnd());

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("CASHFLOW_UPDATE")
                        .data(record));
            } catch (Exception e) {
                log.warn("Lỗi khi gửi SSE tới client, đánh dấu xóa: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            log.info("Đã dọn dẹp {} emitters bị ngắt kết nối.", deadEmitters.size());
        }
    }

    public int getActiveClientCount() {
        return emitters.size();
    }
}
