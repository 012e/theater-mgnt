package com.theatermgnt.theatermgnt.notification.service;

import java.util.Map;

import com.theatermgnt.theatermgnt.notification.enums.EmailType;

public interface EmailTemplateFactory {
    String buildTemplate(EmailType emailType, Map<String, Object> variables);
}
