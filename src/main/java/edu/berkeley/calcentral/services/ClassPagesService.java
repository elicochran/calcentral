package edu.berkeley.calcentral.services;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.ClassPagesDao;
import edu.berkeley.calcentral.daos.ClassPagesLocalDataDao;
import edu.berkeley.calcentral.domain.*;
import edu.berkeley.calcentral.system.Telemetry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.spi.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Service
@Path(Urls.CLASS_PAGES)
public class ClassPagesService {

	private static final Log LOGGER = LogFactory.getLog(ClassPagesService.class);

	@Autowired
	private ClassPagesDao classPagesDao;

	@Autowired
	private UserService userService;

	@Autowired
	private ClassPagesLocalDataDao classPagesLocalDataDao;

	/**
	 * Exposed REST endpoint for fetching classes
	 *
	 * @param ccc concatenated tuple of term_yr, term_cd, and course_catalogue_code
	 * @return course info in an json object, or empty json object on errors
	 */
	@Cache(maxAge = 24 * 60 * 60) // cache for 24 hrs
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{ccc}")
	public Map<String, Object> getClassInfoMap(@PathParam("ccc") String ccc) {
		ClassPage classPage;
		Map<String, Object> returnObject = Maps.newHashMap();
		try {
			classPage= getClassInfo(ccc);
			returnObject.put("classid", classPage.getClassId());
			returnObject.put("info_last_updated", classPage.getInfo_last_updated());
			returnObject.put("courseinfo", classPage.getCourseinfo());
			returnObject.put("classtitle", classPage.getClasstitle());
			returnObject.put("department", classPage.getDepartment());
			returnObject.put("description", classPage.getDescription());
			returnObject.put("instructors", classPage.getInstructors());
			returnObject.put("schedule", classPage.getSchedule());
			returnObject.put("sections", classPage.getSections());
		} catch (Exception e) {
			//TODO: Change this to use whatever final exception handling scheme, instead of swallowing the exception.
			LOGGER.error(e.getClass().getName() + ": " + e.getMessage());
			throw new NotFoundException("Class page could not be found");
		}
		return returnObject;
	}

	public ClassPage getClassInfo(String ccc) {
		//break the crappy smashed up url.
		String year = ccc.substring(0, 4);
		String term = ccc.substring(4, 5);
		String courseID = ccc.substring(5);
		return fetchClassInfo(year, term, courseID);
	}

	private ClassPage fetchClassInfo(String year, String term, String courseID) {
		//sanity check
		if (Strings.nullToEmpty(year).isEmpty()
				|| Strings.nullToEmpty(term).isEmpty()
				|| Strings.nullToEmpty(courseID).isEmpty()) {
			return null;
		}

		int yearInt = Integer.parseInt(year);
		Telemetry telemetry = new Telemetry(this.getClass(), "classPagesDao.getBaseClassPage()");
		ClassPage classPageResult = classPagesDao.getBaseClassPage(yearInt, term, courseID);
		telemetry.end();

		telemetry = new Telemetry(this.getClass(), "classPagesLocalDataDao.mergeLocalData()");
		try {
			classPagesLocalDataDao.mergeLocalData(classPageResult);
		} catch (EmptyResultDataAccessException ignored) {
			// null local classpage data
		}
		telemetry.end();

		classPageResult.getCourseinfo().decodeAll();

		telemetry = new Telemetry(this.getClass(), "classPagesDao.getCourseInstructors()");
		List<ClassPageInstructor> classPageInstructors = classPagesDao.getCourseInstructors(yearInt, term, courseID);
		telemetry.end();
		for (ClassPageInstructor instructor : classPageInstructors) {
			mergeCampusData(instructor);
		}
		classPageResult.setInstructors(classPageInstructors);

		telemetry = new Telemetry(this.getClass(), "classPagesDao.getCourseSchedules()");
		List<ClassPageSchedule> classPageSchedules = classPagesDao.getCourseSchedules(yearInt, term, courseID);
		telemetry.end();
		for (ClassPageSchedule schedule : classPageSchedules) {
			schedule.decodeAll();
		}
		classPageResult.setSchedule(classPageSchedules);

		String deptName = classPageResult.getCourseinfo().getMisc_deptname();
		String catalogId = classPageResult.getCourseinfo().getCatalogid();
		telemetry = new Telemetry(this.getClass(), "classPagesDao.getSectionsWithInstructors()");
		List<ClassPageSection> classPageSections = classPagesDao.getSectionsWithInstructors(yearInt, term, deptName, catalogId);
		telemetry.end();

		telemetry = new Telemetry(this.getClass(), "classPagesDao.merge Campus Data for all Instructors in all sections");
		for ( ClassPageSection section : classPageSections ) {
			for ( ClassPageInstructor instructor : section.getSection_instructors() ) {
				mergeCampusData(instructor);
			}
		}
		telemetry.end();

		//TODO: can probably use predicates here, to get a list of sections we want to keep.
		classPageResult.setSections(classPageSections);

		return classPageResult;
	}

	private void mergeCampusData(ClassPageInstructor instructor) {
		instructor.emailDisclosureDecode();
		User customInstructorFields = userService.getUser(instructor.getId());
		instructor.setUrl(Strings.nullToEmpty(customInstructorFields.getLink()));
		instructor.setImg(Strings.nullToEmpty(customInstructorFields.getProfileImageLink()));
	}

}
