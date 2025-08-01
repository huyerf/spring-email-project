package com.example.email.service;

import com.example.email.model.Login;
//import com.example.email.repo.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserMappingService {

    @Autowired
    private com.example.email.repo.LoginRepository loginRepository;

    public Optional<Login> getAccountByLaoID(String laoEmail) {
        return loginRepository.findByLaoEmail(laoEmail);
    }

    public void saveMapping(String laoEmail, String username, String password) {
        Login mapping = new Login(laoEmail, username, password);
        loginRepository.save(mapping);
    }
}
