package net.orfdev;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UrlMappingsController {
	
	private static final Logger log = LogManager.getLogger();
	
	@Autowired
	private UrlShortDatabase shortUrlDb;

	// --------------------------------------------------------------------------------------------------------------------- //
	// The first set of methods are just here to illustrate how a Spring MVC application can work. The @RequestMapping 
	// annotation is what tells Spring that when a certain URL is requested in a browser it should be mapped onto a specific
	// method here. Spring has several convenient mechanisms for (eg) mapping querystring paramters to method variables, or
	// passing in a Model object, or even HttpServletRequest and HttpServletResponse objects, as variables.
	// You should NOT need to change these methods.
	// --------------------------------------------------------------------------------------------------------------------- //

	private static int INDEX = 0;


	@RequestMapping("/") // defines the URL to access this method - so this will be http://localhost:8080/
	public String hello(
			@RequestParam(defaultValue="World") String who,  // querystring or form parameter with name "who"
			Model model // the Model object is provided by the Spring framework to allow you to pass variables to the template
			) {
		model.addAttribute("who", who);
		return "hello"; // resolves to the HTML template at src/main/resources/templates/hello.html
	}

	
	@RequestMapping("/sam") // defines the URL to access this method - so this will be http://localhost:8080/sam
	public String helloSam() {
		return "redirect:/?who=Sam" ; // using the "redirect:" prefix tells Spring to return a 301 redirect instead of rendering content
	}

	
	@RequestMapping("/sam/{surname}") // defines the URL to access this method - so this will be http://localhost:8080/sam/someValue
	public String helloSamWithSurname(
			@PathVariable(value="surname") String surname // this variable's value is obtained from the RequestMapping, for
				// example if the URL "/sam/foo" was requested the variable's value would be "foo" 
		) {
		return "redirect:/?who=Sam+" + surname; // so eg. http://localhost:8080/sam/I+am would redirect to http://localhost:8080/?who=Sam+I+am
	}
	
	
	@RequestMapping("favicon.ico") // browsers insist on requesting the favicon so just return a 404
	public void favicon(HttpServletResponse response) throws IOException {
		response.sendError(404);
	}

	
	// --------------------------------------------------------------------------------------------------------------------- //
	// The next set of methods are the actual useful thing that do the work of the application.
	// Here is where you may need to add/remove/change code in order to complete the coding task. 
	// --------------------------------------------------------------------------------------------------------------------- //
	
	// Shorten a URL and then show an HTML page
	@RequestMapping("/html/shorten")
	public String shortenUrlAndReturnHtml(@RequestParam(value="url", required=true) String longUrl, Model model) {

		//1. Verify that a URL is passed to the /shorten method
		if(!urlValidator(longUrl)){
			log.debug("The URL [{}]  isn't valid", longUrl);
			throw new RuntimeException();
		}

		String shortUrl = shortenUrl(longUrl);
		model.addAttribute("originalUrl", longUrl);
		model.addAttribute("shortUrl", "http://localhost:8080/" + shortUrl);
		
		return "shortened";
	}

	// --------------------------------------------------------------------------------------------------------------------- //
	// Shorten a URL and then return a JSON payload
	// --------------------------------------------------------------------------------------------------------------------- //
	@RequestMapping("/json/shorten")
	public @ResponseBody Map<String, String> shortenUrlAndReturnJson(@RequestParam(value="url", required=true) String longUrl, Model model) {

		//1. Verify that a URL is passed to the /shorten method
		if(!urlValidator(longUrl)){
			log.debug("The URL [{}]  isn't valid", longUrl);
			throw new RuntimeException();
		}

		String shortUrlPresent = manageDuplicateUrl(longUrl);
		String shortUrl = (shortUrlPresent==null || shortUrlPresent.isEmpty())? shortenUrl(longUrl) :shortUrlPresent;
		
		Map<String, String> payload = new HashMap<>();
		payload.put("originalUrl", longUrl);
		payload.put("shortUrl", "http://localhost:8080/" + shortUrl);
		
		return payload;
	}

	// --------------------------------------------------------------------------------------------------------------------- //
	//
	//	3. Handle duplicate random numbers
	//
	//	Since the short URL is generated by the Base62 encoding of a random number,
	//	it is possible that the code may try to assign the same short URL to two different URLs.
	//	If this happens, the code will currently throw a database exception since the field is the primary key.
	//	Instead, short_url makes the code catches the exception and retries until it 'successful'.
	// --------------------------------------------------------------------------------------------------------------------- //
	private String shortenUrl(String longUrl) {
		boolean successful = false;
		String shortUrl = null;
		INDEX = 1;
		while((!successful) || (INDEX != UtilityCheck.NUM_MAX_TIMES)){
			shortUrl = getShortUrlEncode();
			try {
				shortUrlDb.insert(shortUrl, longUrl);
				successful = true;
				break;
			} catch (Exception ex) {
				successful = false;
				log.info("Operation failed, shortUrl already exist: {}", shortUrl);
				if (ex.getCause() instanceof SQLIntegrityConstraintViolationException) {
					if (!successful) {
						if (INDEX >= UtilityCheck.NUM_MAX_TIMES) {
							String errorMessage = "Caught Exception - Operation failed, shortUrl already exist, we tried for : " + INDEX;
							log.fatal(errorMessage, ex);
							// wrap the exception, e.g. throw new RuntimeException(e);
							throw new RuntimeException(ex);
						} else {
							String errorMessage = "Operation failed, retries remaining: {} ";
							log.info(errorMessage, (UtilityCheck.NUM_MAX_TIMES - (INDEX++)));
							continue;
						}
					} else {
						throw ex;
					}
				} else {
					String errorMessage = "Error while persisting new claims we tried for: " + INDEX;
					log.fatal(errorMessage , ex);
					throw new RuntimeException(ex);
				}

			}
		}

		return shortUrl;
	}

	// --------------------------------------------------------------------------------------------------------------------- //
	// 3. Handle duplicate random numbersBecause
	// the short URL is generated by Base62 encoding a random number, it’s possible that the code
	// can try and assign the same short URL to two different URLs.
	// If this happens the code will currently throw a database exception due to the  field being the primary key. Instead,
	// short_urlmake the code catch the Exception and retry until it is successful.
	// --------------------------------------------------------------------------------------------------------------------- //
	/*
		private String shortenUrl(String longUrl) {
			boolean successful = false;
			String shortUrl = null;
			 do {
				shortUrl = getShortUrlEncode();
				try {
					shortUrlDb.insert(shortUrl, longUrl);
					successful = true;
					break;
				} catch (Exception ex) {
					successful = false;
					if (!successful) {
							log.info("Operation failed, shortUrl already exist: {}", shortUrl);
							shortenUrl(longUrl);
					} else {
						throw ex;
					}
				}
			}	while(!successful);

			return shortUrl;
		}
	*/

	@RequestMapping("/{shortUrl}")
	public String mapUrl(@PathVariable(value="shortUrl") String shortUrl, Model model) {
		
		String longUrl = shortUrlDb.lookupByShortUrl(shortUrl);
		
		if(longUrl == null){
			log.debug("Short url code [{}] not found in DB so returning no content", shortUrl);
			longUrl = "";
		}
		
		return "redirect:" + longUrl;
	}

	// --------------------------------------------------------------------------------------------------------------------- //
	//Assuming that a json object must return, I create an object inside controller this invokes a method internal DAO class,
	// that returns int value , encapsulated into json string to return
	// --------------------------------------------------------------------------------------------------------------------- //
	@RequestMapping(
			value = "/{shortUrl}/health",
			method = RequestMethod.GET,
			produces = UtilityCheck.APPLICATION_JSON_VALUE
	)
	@ResponseBody
	public ResponseEntity<?> healthCheckManagementURL(@PathVariable(value="shortUrl") String shortUrl) {
		log.info("healthCheckManagementURL :  request health check");
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		String response = "";

		try {
			response =  "{\"recordCount\":" + shortUrlDb.getJsonRecordCount(shortUrl)+"}";
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return new ResponseEntity<>(response, httpHeaders, HttpStatus.OK);
	}

	// --------------------------------------------------------------------------------------------------------------------- //
	// URL regex that starts with HTTP or HTTPS
	// The HTTP and HTTPS URLs should start with the following protocol which I test using the following regular expression
	// --------------------------------------------------------------------------------------------------------------------- //
	private boolean httpMatching(String url){

		return Pattern.compile(UtilityCheck.HTTP_HTTPS_URL_REGEX)
				.matcher(url)
				.find();
	}

	// --------------------------------------------------------------------------------------------------------------------- //
	// The URL is a maximum of 256 characters in length
	// --------------------------------------------------------------------------------------------------------------------- //
	private boolean urlValidationSize(String url)
	{
		return (url.length()<=UtilityCheck.LIMIT_URL) ;
	}


	private String getDomainName(String url){
		String domainName = null;
		if (url == null) return domainName;

		url = url.trim();
		Matcher matcher = UtilityCheck.HOST_EXTRACTOR_REGEX_PATTERN.matcher(url);
		domainName = (matcher.find() && matcher.groupCount() == 2) ? matcher.group(1) + matcher.group(2) : null;
		return domainName;
	}



	// --------------------------------------------------------------------------------------------------------------------- //
	// the domain is just a single top level domain (so for example it is not a valid URL but it is a valid URL)
	// --------------------------------------------------------------------------------------------------------------------- //
	private boolean urlMatching(String url){
		// the domain is just a single top level domain (so for example it is not a valid URL but it is a valid URL)

        //Find out if there are any occurrences of the word "orpheussoftware" in a sentence:
		boolean matchFound = UtilityCheck.pattern.matcher(getDomainName(url)).find();
		return  (Pattern.compile(UtilityCheck.URL_REGEX)
				.matcher(url)
				.find() && (matchFound));
	}



	// --------------------------------------------------------------------------------------------------------------------- //
	// 1. Validate that a URL is being passed to the /shorten method
	// --------------------------------------------------------------------------------------------------------------------- //
	private boolean urlValidator(String url)
	{
		return (httpMatching(url) && urlMatching(url) && (urlValidationSize(url))) ;
	}
	private String getShortUrlEncode(){
		long random = new Random().nextLong();
		if(random < 0) random = random * -1;
		return Base62.encode(random);
	}

	// --------------------------------------------------------------------------------------------------------------------- //
	// 2. Manage duplicate URLs
	// --------------------------------------------------------------------------------------------------------------------- //
	private String manageDuplicateUrl(String longUrl)
	{
		return shortUrlDb.lookupByLongUrl(longUrl);
	}

}