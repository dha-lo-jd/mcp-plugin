package org.lo.d.site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

@Controller
public class Index extends AbstractThymlPage {

	private static final Section[] sections = SimpleSection.newSimpleSections("index", "outline", "updates", "license", "install", "get_start");

	@Autowired
	@Qualifier("contents.updates")
	private Content updates;

	public Index() {
		super("index", sections);
	}

	public Content getUpdates() {
		return updates;
	}

	@Override
	public String outputDirectory() {
		return ".";
	}
}
