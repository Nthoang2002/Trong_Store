package com.tuyenvp.spring_boot_app.Controller;

import com.tuyenvp.spring_boot_app.DTO.PaymentDTO;
import com.tuyenvp.spring_boot_app.Response.ResponseObject;
import com.tuyenvp.spring_boot_app.Services.Impl.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class VNPayController {
    private final VNPayService paymentService;
    @GetMapping("/vn-pay")
    public ResponseObject<PaymentDTO.VNPayResponse> pay(
            HttpServletRequest request,
            @RequestParam("amount") long amount,
            @RequestParam(value = "bankCode", required = false) String bankCode
            ) {
        return new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createVnPayPayment(amount, bankCode, request));
    }
    

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");

        if ("00".equals(vnp_ResponseCode)) {
            model.addAttribute("message", "Thanh toán thành công!");
        } else {
            model.addAttribute("message", "Thanh toán không thành công!");
        }

        return "user/payment-status"; // Đúng đường dẫn của file HTML trong thư mục templates/user
    }
}