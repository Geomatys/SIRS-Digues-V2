package fr.symadrem.sirs.core.component;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.symadrem.sirs.core.SymadremCore;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.Element;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test/test-context.xml")
public class DocumentChangeEmiterTestCase implements DocumentListener {

	@Autowired
	private DocumentChangeEmiter documentChangeEmiter;

	@Autowired
	private DigueRepository digueRepository;

	@PostConstruct
	public void init() {
		documentChangeEmiter.addListener(this);
	}

	@Test
	public void testListen() throws InterruptedException {

		Digue digue = new Digue();
		digueRepository.add(digue);
		digue.setCommentaire("zozo");
		digueRepository.update(digue);
		digueRepository.remove(digue);
		Thread.sleep(10000);
	}

	@Override
	public Element documentDeleted(Element element) {
		SymadremCore.LOGGER.info("documentDeleted(" + element + ")");
		return element;
	}

	@Override
	public Element documentChanged(Element element) {
		SymadremCore.LOGGER.info("documentChanged(" + element + ")");
		return element;
	}

	@Override
	public Element documentCreated(Element changed) {
		SymadremCore.LOGGER.info("documentCreated(" + changed + ")");
		return changed;
	}

}
