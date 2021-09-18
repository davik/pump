package info.kalyan.krishi.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
class LoginController {

	@Value("${app.welcome.title}")
	private String title = "";

	@GetMapping
	public String form(Map<String, Object> model) {
		model.put("title", title);
		model.put("message", "");
		return "login";
	}

}