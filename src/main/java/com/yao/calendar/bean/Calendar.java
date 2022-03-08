package com.yao.calendar.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @ClassName : Calendar
 * @Description :
 * @Author : felix
 * @Date: 2022-03-07 16:26
 */

@TableName(value = "calendar")
@Data
public class Calendar {

//	@TableId(type = IdType.AUTO)
	private int id;

	public String year;

	public String month;

	public String day;

	public String date;

	public String week;

	public String lunarYear;

	public String lunarMonth;

	public String lunarDay;

	public String lunarDate;

	public String restDay;

}
