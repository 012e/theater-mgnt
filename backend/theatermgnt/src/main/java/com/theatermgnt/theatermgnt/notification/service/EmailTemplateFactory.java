package com.theatermgnt.theatermgnt.notification.service;

import com.theatermgnt.theatermgnt.notification.enums.EmailType;

import java.util.Map;

public interface EmailTemplateFactory {
    String buildTemplate(EmailType emailType, Map<String, Object> variables);
}
