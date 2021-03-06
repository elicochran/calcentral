package edu.berkeley.calcentral.daos;

import com.google.common.collect.Maps;
import edu.berkeley.calcentral.domain.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Repository
public class ClassPagesDao extends BaseDao {

	public static String[] getSectionIDParts(String sectionID) {
		String[] sectionIDParts = sectionID.split("-");
		if (sectionIDParts.length != 3) {
			throw new IllegalArgumentException("Got a section ID that doesn't conform to format: " + sectionID);
		}
		return sectionIDParts;
	}

	public ClassPage getBaseClassPage(int year, String term, String courseID) {
		MapSqlParameterSource params = setupParams(year, term, courseID)
				.addValue("primary", "P")
				.addValue("format", "IND");
		String rootInfo = " SELECT "
				+ " bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, " //ignoring course control num permissions issues for now.
				+ " '' info_last_updated, "
				+ " bci.COURSE_TITLE classtitle, "
				+ " bci.COURSE_TITLE title, "
				+ " bci.INSTRUCTION_FORMAT format,"
				+ " bci.CRED_CD grading, "
				+ " '' prereqs, "
				+ " '' requirements, "
				+ " bci.term_cd term, "
				+ " '' semesters_offered, "
				+ " bci.term_yr year, "
				+ " bci.DEPT_DESCRIPTION department, "
				+ " bci.DEPT_NAME misc_deptname, "
				+ " bci.SCHEDULE_PRINT_CD misc_scheduleprintcd, "
				+ " bci.COURSE_CNTL_NUM coursenum, "
				+ " bci.FIXED_UNIT misc_fixedunit, "
				+ " bci.LOWER_RANGE_UNIT misc_lowerRangeUnit, "
				+ " bci.UPPER_RANGE_UNIT misc_upperRangeUnit, "
				+ " bci.VARIABLE_UNIT_CD misc_variableUnitCd, "
				+ " '' units, "
				+ " bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, " //ignoring course control num permissions issues for now.
				+ " bci.COURSE_TITLE classtitle, "
				+ " bci.CATALOG_DESCRIPTION description, "
				+ " bci.CATALOG_ID catalogid "
				+ " FROM BSPACE_COURSE_INFO_VW bci "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID "
				+ "   AND bci.PRIMARY_SECONDARY_CD = :primary"
				+ "   AND bci.INSTRUCTION_FORMAT <> :format ";
		return campusQueryRunner.queryForObject(rootInfo, params, classPageMapper);
	}

	public List<ClassPageInstructor> getCourseInstructors(int year, String term, String courseID) {
		MapSqlParameterSource params = setupParams(year, term, courseID);
		String instructors = " SELECT "
				+ "   bpi.email_address email, "
				+ "   bpi.ldap_uid id, "
				+ "   bpi.person_name name, "
				+ "   bpi.email_disclos_cd misc_email_disclosure,"
				+ "   '' office, "
				+ "   bpi.telephone phone, "
				+ "   '' img, "
				+ "   '' title, "
				+ "   '' url "
				+ " FROM BSPACE_PERSON_INFO_VW bpi, BSPACE_COURSE_INSTRUCTOR_VW bci "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID "
				+ "   AND bpi.ldap_uid = bci.instructor_ldap_uid"
				+ " ORDER BY bci.MULTI_ENTRY_CD";
		return campusQueryRunner.query(instructors, params,
				new BeanPropertyRowMapper<ClassPageInstructor>(ClassPageInstructor.class));
	}

	public List<ClassPageSchedule> getCourseSchedules(int year, String term, String courseID) {
		MapSqlParameterSource params = setupParams(year, term, courseID);
		//There's some ambiguous logic related to this. Not worrying about edge cases for the time being.
		//disregarding the online schedule of classes rules for now.
		String schedule = " SELECT "
				+ " '' coords, "
				+ " trim(nvl(bcs.ROOM_NUMBER, '')) misc_room, "
				+ " trim(nvl(bcs.BUILDING_NAME, '')) misc_building_name, "
				+ " '' location, "
				+ " bcs.meeting_start_time || bcs.meeting_start_time_ampm_flag || '-' || bcs.meeting_end_time || bcs.meeting_end_time_ampm_flag time, "
				+ " bcs.MEETING_DAYS misc_weekdays, "
				+ " '' weekdays, "
				+ " '' current_sem "
				+ " FROM BSPACE_CLASS_SCHEDULE_VW bcs "
				+ " WHERE (bcs.TERM_YR = :year AND bcs.TERM_CD = :term AND bcs.COURSE_CNTL_NUM = :courseID"
				+ "   AND (trim(bcs.MEETING_DAYS) != 'UNSCHED' AND trim(bcs.BUILDING_NAME) != 'NO FACILITY')) ";
		return campusQueryRunner.query(schedule, params,
				new BeanPropertyRowMapper<ClassPageSchedule>(ClassPageSchedule.class));
	}

	public List<ClassPageSection> getSectionsWithInstructors(int year, String term, String deptName, String catalogId) {
		MapSqlParameterSource params = setupParams(year, term, "")
				.addValue("deptName", deptName)
				.addValue("catalogId", catalogId);
		String sql = "SELECT" +
				"  bci.COURSE_CNTL_NUM ccn," +
				"  '' enrolled_cur," +
				"  bci.ENROLL_LIMIT enrolled_max," +
				"  trim(nvl(bcs.ROOM_NUMBER, '')) misc_room," +
				"  trim(nvl(bcs.BUILDING_NAME, '')) misc_building_name," +
				"  '' location," +
				"  '' coords," +
				"  '' note," +
				"  bci.INSTRUCTION_FORMAT || ' ' || bci.SECTION_NUM section," +
				"  bcs.meeting_start_time || bcs.meeting_start_time_ampm_flag || '-' || bcs.meeting_end_time || bcs.meeting_end_time_ampm_flag time," +
				"  bcs.MEETING_DAYS misc_weekdays," +
				"  '' weekdays," +
				"  '' waitlist," +
				"  '' midterm_datetime," +
				"  '' midterm_location," +
				"  '' midterm_coords," +
				"  '' midterm_note," +
				"  '' final_datetime," +
				"  '' final_location," +
				"  '' final_coords," +
				"  '' final_note," +
				"  '' restrictions," +
				"  bpi.email_address email," +
				"  bpi.ldap_uid id," +
				"  bpi.person_name name," +
				"  bpi.email_disclos_cd misc_email_disclosure," +
				"  '' office," +
				"  bpi.telephone phone," +
				"  '' img," +
				"  '' title," +
				"  '' url" +
				"  FROM BSPACE_COURSE_INFO_VW bci" +
				"  LEFT JOIN BSPACE_CLASS_SCHEDULE_VW bcs on" +
				"  (bcs.TERM_YR = bci.TERM_YR AND bcs.TERM_CD = bci.TERM_CD AND bcs.COURSE_CNTL_NUM = bci.COURSE_CNTL_NUM" +
				"  AND (trim(bcs.MEETING_DAYS) != 'UNSCHED' AND trim(bcs.BUILDING_NAME) != 'NO FACILITY'))" +
				"  LEFT JOIN BSPACE_COURSE_INSTRUCTOR_VW bcci on ( bcci.TERM_YR = bci.TERM_YR AND bcci.TERM_CD = bci.TERM_CD AND bcci.COURSE_CNTL_NUM = bci.COURSE_CNTL_NUM )" +
				"  JOIN BSPACE_PERSON_INFO_VW bpi on (bpi.ldap_uid = bcci.instructor_ldap_uid)" +
				"  WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term" +
				"  AND bci.DEPT_NAME = :deptName AND bci.CATALOG_ID = :catalogId" +
				"  AND NVL(bci.SECTION_CANCEL_FLAG, 'N') <> 'Y'" +
				"  ORDER BY bci.PRIMARY_SECONDARY_CD, section, bcci.MULTI_ENTRY_CD";
		return campusQueryRunner.query(sql, params, sectionExtractor);
	}

	private ResultSetExtractor<List<ClassPageSection>> sectionExtractor = new ResultSetExtractor<List<ClassPageSection>>() {

		private BeanPropertyRowMapper<ClassPageInstructor> instructorMapper = new BeanPropertyRowMapper<ClassPageInstructor>(ClassPageInstructor.class);

		private RowMapper<ClassPageSection> sectionMapper = new RowMapper<ClassPageSection>() {
			private BeanPropertyRowMapper<ClassPageSection> baseSectionMapper = new BeanPropertyRowMapper<ClassPageSection>(ClassPageSection.class);
			private BeanPropertyRowMapper<ClassPageSchedule> scheduleMapper = new BeanPropertyRowMapper<ClassPageSchedule>(ClassPageSchedule.class);

			public ClassPageSection mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				ClassPageSection classPageSection = baseSectionMapper.mapRow(rs, rowNum);
				ClassPageSchedule sectionSchedule = scheduleMapper.mapRow(rs, rowNum);
				sectionSchedule.decodeAll();
				classPageSection.setMisc_schedule(sectionSchedule);
				return classPageSection;
			}
		};

		public List<ClassPageSection> extractData(ResultSet rs) throws SQLException, DataAccessException {
			LinkedHashMap<String, ClassPageSection> sections = Maps.newLinkedHashMap();
			while (rs.next()) {
				String ccn = rs.getString("ccn");
				ClassPageSection section = sections.get(ccn);
				if (section == null) {
					section = sectionMapper.mapRow(rs, rs.getRow());
					section.setSection_instructors(new ArrayList<ClassPageInstructor>());
					sections.put(ccn, section);
				}
				List<ClassPageInstructor> instructors = section.getSection_instructors();
				instructors.add(instructorMapper.mapRow(rs, rs.getRow()));
			}
			return new ArrayList<ClassPageSection>(sections.values());
		}
	};

	private RowMapper<ClassPage> classPageMapper = new RowMapper<ClassPage>() {

		private BeanPropertyRowMapper<ClassPage> baseClassPageMapper = new BeanPropertyRowMapper<ClassPage>(ClassPage.class);
		private BeanPropertyRowMapper<ClassPageCourseInfo> courseInfoMapper = new BeanPropertyRowMapper<ClassPageCourseInfo>(ClassPageCourseInfo.class);

		public ClassPage mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClassPage classPage = baseClassPageMapper.mapRow(rs, rowNum);
			ClassPageCourseInfo courseInfo = courseInfoMapper.mapRow(rs, rowNum);
			classPage.setCourseinfo(courseInfo);
			return classPage;
		}
	};

	private MapSqlParameterSource setupParams(int year, String term, String courseID) {
		return new MapSqlParameterSource()
				.addValue("year", year)
				.addValue("term", term)
				.addValue("courseID", courseID);
	}

}
