package net.orfdev;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * This class is just a crude way of doing very simple DDL and CRUD on a SQL database 
 */
@Component
public class UrlShortDatabase {
	
	@Autowired
	private JdbcTemplate jdbctemplate;
	
	
	@PostConstruct
	public void constructDB() {
		jdbctemplate.execute("CREATE TABLE If Not Exists urls (short_url varchar(32) Primary Key, long_url varchar(256));");
	}
	
	
	public void insert(String base62Number, String longUrl) {	
		jdbctemplate.update("INSERT INTO urls (short_url, long_url) Values (?,?)", base62Number, longUrl);
	}

	
	public String lookupByShortUrl(String shortUrl) {
		List<String> results = jdbctemplate.queryForList("SELECT long_url FROM urls WHERE short_url = ?", String.class, shortUrl);
		if(results.isEmpty()){
			return null;
		}
		return results.get(0);
	}

	public String lookupByLongUrl(String longUrl) {
		List<String> results = jdbctemplate.queryForList("SELECT  short_url FROM urls WHERE long_url = ?", String.class, longUrl);
		if(results.isEmpty()){
			return null;
		}
		return results.get(0);
	}

	public int getCountShortUrl(String shortUrl) {
		int count = jdbctemplate.queryForObject("SELECT COUNT(*) FROM urls WHERE short_url = ?", Integer.class, shortUrl);
		return count;
	}

	public int getJsonRecordCount(String shortUrl) throws JsonProcessingException {
		int response =  getCountShortUrl(shortUrl);
		return response;
	}


}