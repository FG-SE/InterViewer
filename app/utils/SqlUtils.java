package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONStringer;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

public class SqlUtils {
	/*
	 * TODO: Discuss with costumer
	 */
	private static final DirectQueryManagement DIRECT_QUERYS = DirectQueryManagement.allow;

	private enum DirectQueryManagement {
		allow, prohibit
	}

	protected static List<String> directQueryKeys(String sqlQueryString) {
		if (DIRECT_QUERYS == DirectQueryManagement.allow) {
			SqlQuery sqlQuery = Ebean.createSqlQuery(sqlQueryString);

			List<SqlRow> sqlRowList = sqlQuery.findList();

			List<String> head = new ArrayList<String>();
			Iterator<String> iterator = sqlRowList.get(0).keys();
			while (iterator.hasNext()){
				head.add(iterator.next());
			}
			if(head.contains("password")){
				head.remove("password");
			}
			return head;
		} else {
			throw new SecurityException("This operation is not allowed");
		}
	}

	protected static List<List<String>> directQueryValues(String sqlQueryString) {
		if (DIRECT_QUERYS == DirectQueryManagement.allow) {
			SqlQuery sqlQuery = Ebean.createSqlQuery(sqlQueryString);

			List<SqlRow> sqlRowList = sqlQuery.findList();

			List<String> head = directQueryKeys(sqlQueryString);

			List<List<String>> result = new ArrayList<List<String>>();

			for (SqlRow s : sqlRowList) {

				List<String> row = new ArrayList<String>();
				for (String h : head) {
					Object o = s.get(h); 
					if (o == null) {
						o = "null";
					}
					row.add(o.toString());
				}
				result.add(row);

			}

			return result;

		} else {
			throw new SecurityException("This operation is not allowed");
		}
	}

	public static JSONStringer getDirectQueryStandardJSON(String sqlQueryString) {
		JSONStringer json = new JSONStringer();

		List<String> rawKeys = directQueryKeys(sqlQueryString);
		List<List<String>> rawVaules = directQueryValues(sqlQueryString);

		try {
			json.object();

			json.key("values").array();

			for (List<String> valueRow : rawVaules) {
				json.object();
				for (int i = 0; i < rawKeys.size(); i++) {
					json.key(rawKeys.get(i)).value(valueRow.get(i));
				}
				json.endObject();
			}

			json.endArray();
			json.endObject();
		} catch (JSONException e) {
			return new JSONStringer();
		}
		return json;
	}
	
	public static JSONStringer getDirectQueryMustacheJSON(String sqlQueryString) {
		JSONStringer json = new JSONStringer();

		List<String> rawKeys = directQueryKeys(sqlQueryString);
		List<List<String>> rawVaules = directQueryValues(sqlQueryString);

		try {
			json.object();

			json.key("keys").array();

			for (String key : rawKeys) {

				json.object();

				json.key("key").value(key);

				json.endObject();
			}

			json.endArray();

			json.key("valueRows").array();

			for (List<String> valueRow : rawVaules) {
				json.object();
				json.key("values").array();
				for (int i = 0; i < rawKeys.size(); i++) {
					if (rawKeys.get(i).contains("password")) {
						continue;
					}
					json.object();
					json.key("value").value(valueRow.get(i));
					json.endObject();
				}
				json.endArray();
				json.endObject();
			}

			json.endArray();
			json.endObject();
		} catch (JSONException e) {
			return new JSONStringer();
		}
		return json;
	}
}
