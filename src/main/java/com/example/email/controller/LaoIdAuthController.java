package com.example.email.controller;

import com.example.email.model.Login;
import com.example.email.service.AuthService;
import com.example.email.service.UserMappingService;
//import com.example.email.session.MailSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class LaoIdAuthController {

    @Autowired
    private UserMappingService userMappingService;

    @Autowired
    private AuthService authService;

    @GetMapping("/laoid/auth/callback")
    public String handleCallback(@RequestParam("authorization_code") String code, HttpSession session) {
        try {
            String clientId = "660dfa27-5a95-4c88-8a55-abe1310bf579"; // từ LaoID
            String clientSecret = "df1699140bcb456eaa6d85d54c5fbd79";     // từ LaoID

            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of(
                "code", code,
                "clientId", clientId,
                "clientSecret", clientSecret
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = rest.postForEntity(
                "https://demo-sso.tinasoft.io/api/v1/third-party/verify", request, Map.class);

            if (!Boolean.TRUE.equals(response.getBody().get("success"))) {
                return "redirect:/index.html?login=fail";
            }

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            String token = (String) data.get("accessToken");

            // Lấy thông tin người dùng
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + token);
            userHeaders.set("x-api-key", clientId);

            HttpEntity<Void> userReq = new HttpEntity<>(userHeaders);
            ResponseEntity<Map> userRes = rest.exchange(
                "https://demo-sso.tinasoft.io/api/v1/third-party/me",
                HttpMethod.GET,
                userReq,
                Map.class);

            if (!Boolean.TRUE.equals(userRes.getBody().get("success"))) {
                return "redirect:/index.html?login=fail";
            }

            Map<String, Object> userData = (Map<String, Object>) userRes.getBody().get("data");
            List<Map<String, Object>> emailList = (List<Map<String, Object>>) userData.get("email");
            String laoEmail = (String) emailList.get(0).get("email");

            Optional<Login> accOpt = userMappingService.getAccountByLaoID(laoEmail);
            if (accOpt.isPresent()) {
                Login acc = accOpt.get();
                boolean ok = authService.login(acc.getUsername(), acc.getPassword());
                if (!ok) return "redirect:/index.html?login=fail";

                session.setAttribute("username", acc.getUsername());
                session.setAttribute("store", authService.getSession(acc.getUsername()).getStore());
                return "redirect:/inbox.html";
            } else {
                session.setAttribute("pending_laoid", laoEmail);
                return "redirect:/bind.html";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/index.html?login=error";
        }
    }

    @PostMapping("/bind")
    public String bindAccount(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session) {
        String laoEmail = (String) session.getAttribute("pending_laoid");

        boolean ok = authService.login(username, password);
        if (!ok) return "redirect:/bind.html?login=fail";

        userMappingService.saveMapping(laoEmail, username, password);
        session.setAttribute("username", username);
        session.setAttribute("store", authService.getSession(username).getStore());
        return "redirect:/inbox.html";
    }
}
