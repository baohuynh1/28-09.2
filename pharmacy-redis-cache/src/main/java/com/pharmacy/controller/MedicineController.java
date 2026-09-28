package com.pharmacy.controller;

import com.pharmacy.entity.Medicine;
import com.pharmacy.service.MedicineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private static final Logger log = LoggerFactory.getLogger(MedicineController.class);
    private final MedicineService service;

    public MedicineController(MedicineService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getById(@PathVariable Long id) {
        long start = System.nanoTime();
        Medicine m = service.getMedicineById(id);
        double ms = (System.nanoTime() - start) / 1_000_000.0;
        log.info("GET /api/medicines/{} xử lý trong {} ms", id, String.format("%.2f", ms));
        return ResponseEntity.ok()
                .header("X-Response-Time-ms", String.format("%.2f", ms))
                .body(m);
    }

    @PostMapping
    public Medicine create(@RequestBody Medicine m) {
        return service.create(m);
    }

    /** Cập nhật thông tin thuốc (chỉ các trường gửi lên). */
    @PutMapping("/{id}")
    public Medicine update(@PathVariable Long id, @RequestBody Medicine m) {
        m.setId(id);                       // để #medicine.id = id trên URL
        return service.updateMedicine(m);
    }

    /** Sửa riêng giá: PATCH /api/medicines/1/price  body: {"price": 99000} */
    @PatchMapping("/{id}/price")
    public Medicine updatePrice(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        Medicine m = new Medicine();
        m.setId(id);
        m.setPrice(body.get("price"));
        return service.updateMedicine(m);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }
}
