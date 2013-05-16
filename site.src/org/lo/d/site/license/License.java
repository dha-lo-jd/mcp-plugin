package org.lo.d.site.license;

import org.lo.d.site.AbstractThymlPage;
import org.lo.d.site.Section;
import org.lo.d.site.SimpleSection;
import org.springframework.stereotype.Controller;

@Controller
public class License extends AbstractThymlPage {

	private static final Section[] sections = SimpleSection.newSimpleSections("license");

	public License() {
		super("license", sections);
	}

	@Override
	public String outputName() {
		return "index";
	}

}
