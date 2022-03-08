package com.yao.calendar.task;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.yao.calendar.bean.Calendar;
import com.yao.calendar.service.CalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * @ClassName : Calendar
 * @Description : 日历
 * @Author : felix
 * @Date: 2022-03-07 16:22
 */


@Slf4j
@Component
public class CalendarTask {

	@Autowired
	private CalendarService calendarService;


	@PostConstruct
	public void getCalendarData(){

		int year  = LocalDate.now().getYear();

		int addYear = 1;

		int endYear = year+addYear;

		while (year<=endYear) {

			for (int i = 1; i <= 12; i++) {
//			for (int i = 5; i <= 5; i++) {

				ArrayList<Calendar> calendars = get(String.valueOf(year), String.valueOf(i));

				calendarService.saveOrUpdateBatch(calendars);

				log.info("{}年{}月",year,i);
			}

			year++;
		}

		getHolidayData();
	}

	/**
	 * @description 获取对应年份，月份的日历数据
	 * @author felixYao
	 * @date 2022/3/8 17:56
	 * @Param  [year, month]
	 * @return java.util.ArrayList<com.felix.weixin.bean.Calendar>
	 * @throws
	 */
	public static ArrayList<Calendar> get(String year,String month){

		String url = "https://www.rili.com.cn/rili/json/pc_wnl/year/month.js?_=time"
				.replace("year",    year)
				.replace("month",   month)
				.replace("time",    String.valueOf(System.currentTimeMillis()));
				;

		String s = HttpUtil.get(url);

		String replace = s.replace("jsonrun_PcWnl(", "").replace(",\"js\");", "");

		JSONObject jsonObject = JSONObject.parseObject(replace);

		JSONArray data = jsonObject.getJSONArray("data");


		ArrayList<Calendar> arrayList = new ArrayList<>();

		data.stream().forEach(a -> {

			JSONObject date = (JSONObject) a;

			StringBuilder sb = new StringBuilder();

			Calendar calendar = new Calendar();
			calendar.setYear(date.getString("nian"));
			calendar.setMonth(date.getString("yue"));
			calendar.setDay(date.getString("ri"));
			calendar.setWeek(date.getString("week"));

			calendar.setLunarDay(date.getString("r2"));
			calendar.setLunarMonth(date.getString("n_yueri").replace(calendar.getLunarDay(),""));
			calendar.setLunarDate(date.getString("n_yueri"));
			calendar.setLunarYear(date.getString("gz_nian"));

			calendar.setRestDay((date.getIntValue("week")==6 || date.getIntValue("week")==7)?"1":"0");

			calendar.setDate( sb.append(calendar.getYear())
					.append( calendar.getMonth().length()==1? "0".concat(calendar.getMonth()) : calendar.getMonth() )
					.append( calendar.getDay().length()==1? "0".concat(calendar.getDay()) : calendar.getDay() )
							.toString()
			);

			calendar.setId(Integer.valueOf(calendar.getDate()));

			arrayList.add(calendar);

			log.info(calendar.toString());

		});

		return arrayList;

	}


	/**
	 * @description 获取休息日数据并更新数据库
	 * @author felixYao
	 * @date 2022/3/8 17:54
	 * @Param  []
	 * @return void
	 * @throws
	 */
	public void getHolidayData(){

		ArrayList<Calendar> calendarList = new ArrayList<>();

		String url = "https://qq.ip138.com/static/script/day/index.js";

		String s = HttpUtil.get(url);

		String substring = s.substring(s.indexOf("_holiday = {")+11);

		String substring1 = substring.substring(0, substring.indexOf(";"));

		JSONObject jsonObject = JSONObject.parseObject(substring1, Feature.OrderedField);

		jsonObject.entrySet().forEach(a -> {

			String key = a.getKey();
			String value = a.getValue().toString();

			int year = LocalDate.now().getYear();

			//获取最新年份休息日数据
			if (Integer.parseInt(key)>=year)
				calendarList.addAll(getHolidayDataByYear(key, value));
		});

		calendarService.saveOrUpdateBatch(calendarList);

	}

	/**
	 * @description 解析对应年份的休息日和调休上班数据
	 * @author felixYao
	 * @date 2022/3/8 17:55
	 * @Param  [year, data]
	 * @return java.util.ArrayList<com.felix.weixin.bean.Calendar>
	 * @throws
	 */
	private static ArrayList<Calendar> getHolidayDataByYear(String year, String data){

		ArrayList<Calendar> calendarList = new ArrayList<>();

		JSONObject jsonObject = JSONObject.parseObject(data, Feature.OrderedField);

		jsonObject.entrySet().forEach(a->{

			Calendar calendar = new Calendar();

			String date = year.concat(a.getKey());
			String restDay = a.getValue().toString();

			calendar.setId(Integer.parseInt(date));
			calendar.setRestDay(restDay);
			calendarList.add(calendar);
		});

		return calendarList;
	}


}
