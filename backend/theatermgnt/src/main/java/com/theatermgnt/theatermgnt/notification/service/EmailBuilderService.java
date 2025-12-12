package com.theatermgnt.theatermgnt.notification.service;

import com.theatermgnt.theatermgnt.notification.dto.request.EmailBuilderRequest;

public interface EmailBuilderService {
    void buildAndSendEmail(EmailBuilderRequest request);
}
