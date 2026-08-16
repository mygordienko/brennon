package com.mygordienko.brennon.core.common.adapter;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorPageController {
    @RequestMapping(value = "/404", produces = MediaType.TEXT_HTML_VALUE)
    public String handle404() {
        return "core/common/404";
    }
}
