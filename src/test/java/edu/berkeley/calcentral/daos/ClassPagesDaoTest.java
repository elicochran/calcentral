package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.DatabaseAwareTest;
import edu.berkeley.calcentral.domain.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ClassPagesDaoTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(ClassPagesDaoTest.class);
	private static final int YEAR = 2012;
	private static final String TERM = "D";
	private static final String COURSE_ID = "7308";

	@Autowired
	private ClassPagesDao dao;

	@Test
	public void getBaseClassPage() throws Exception {
		ClassPage page = dao.getBaseClassPage(YEAR, TERM, COURSE_ID);
		assertNotNull(page);
		assertEquals("General Biology Lecture", page.getClasstitle());
		assertEquals("Biology", page.getDepartment());
		assertEquals("1A", page.getCatalogid());
		ClassPageCourseInfo info = dao.getBaseClassPage(YEAR, TERM, COURSE_ID).getCourseinfo();
		assertNotNull(info);
		assertEquals("LEC", info.getFormat());
		assertEquals("BIOLOGY", info.getMisc_deptname());
	}

	@Test
	public void getCourseInstructors() throws Exception {
		List<ClassPageInstructor> instructors = dao.getCourseInstructors(YEAR, TERM, COURSE_ID);
		assertEquals(1, instructors.size());
		assertNotNull(instructors.get(0).getEmail());
	}

	@Test
	public void getCourseSchedules() throws Exception {
		List<ClassPageSchedule> schedules = dao.getCourseSchedules(YEAR, TERM, COURSE_ID);
		assertEquals(1, schedules.size());
	}

	@Test
	public void getSectionsWithInstructors() throws Exception {
		List<ClassPageSection> sections = dao.getSectionsWithInstructors(YEAR, TERM, "BIOLOGY", "1A");
		assertTrue(sections.size() > 0);
		ClassPageSection first = sections.get(0);
		assertTrue(first.getSection_instructors().size() > 0);
	}
}
