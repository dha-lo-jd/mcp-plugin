package org.lo.d.site;

import org.springframework.stereotype.Controller;

@Controller
public class Index extends AbstractThymlPage {

	private static final Section[] sections = SimpleSection.newSimpleSections("index", "outline", "license", "install", "get_start");

	public Index() {
		super("index", sections);
	}

	@Override
	public String outputDirectory() {
		return ".";
	}
}
