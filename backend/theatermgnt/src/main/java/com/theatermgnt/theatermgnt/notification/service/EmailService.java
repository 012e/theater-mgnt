package com.theatermgnt.theatermgnt.notification.service;

import com.theatermgnt.theatermgnt.notification.dto.request.SendEmailRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.EmailResponse;

public interface EmailService {
    EmailResponse sendEmail(SendEmailRequest request);
}
