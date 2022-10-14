package net.orfdev;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class })
@SpringBootTest
@AutoConfigureMockMvc
class UrlMappingsControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void setup() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void testHello() throws Exception {
        String htmlReponse= "<!DOCTYPE HTML>\n<html>\n<head>\n    <title>Welcome</title>\n</head>\n<body>\n    <p>Hello <span>World</span></p>\n</body>\n</html>\n";
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(htmlReponse)));
    }

    @Test
    public void testHelloSam() throws Exception {
        String htmlReponse= "<!DOCTYPE HTML>\n<html>\n<head>\n    <title>Welcome</title>\n</head>\n<body>\n    <p>Hello <span>Sam</span></p>\n</body>\n</html>\n";

        mvc.perform(get("/?who=Sam"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(htmlReponse)));
    }



    @Test
    public void testShortenUrlAndReturnHtml() throws Exception {
        String request= "/html/shorten?url=https://www.orpheussoftware.co.uk/about/90c2f90903g3q870debby21G31386g7e";
        String response= "<!DOCTYPE HTML>\n<html>\n<head>\n<title>Short URL</title>\n</head>\n<body>\n<p>Original URL:</p>\n<pre>https://www.orpheussoftware.co.uk/about/90c2f90903g3q870debby21G31386g7e</pre>\n<p>Short URL:</p>\n<pre>http://localhost:8080/6DxFPGmytbZ</pre>\n</body>\n</html>\n";

        ResultActions resultActions =  mvc.perform(get(request))
               .andExpect(status().isOk());



        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();

        String responseTitle = "<title>Short URL</title>";
        assertTrue(responseBody.contains(responseTitle));

        String responseContainOriginalURL = "<p>Original URL:</p>";
        assertTrue(responseBody.contains(responseContainOriginalURL));

        String responseContain = "<pre>https://www.orpheussoftware.co.uk/about/90c2f90903g3q870debby21G31386g7e</pre>";
        assertTrue(responseBody.contains(responseContain));

        String responseUrl="<pre>http://localhost:8080/";
        assertTrue(responseBody.contains(responseUrl));
        String responseShortURLContain = "<p>Short URL:</p>";
        assertTrue(responseBody.contains(responseShortURLContain));

        String responseEnd = "</body>\n</html>";
        assertTrue(responseBody.contains(responseEnd));
    }


    //{
    //    "shortUrl": "http://localhost:8080/2uKEUxwqONA",
    //    "originalUrl": "https://www.orpheussoftware.co.uk/about/90c2f90903g3q870debby21G31386g7e"
    //}


    @Test
    public void testHealthCheckManagementURL() throws Exception {
        String request = "/{shortUrl}/health";
        String shortUrl = "3zyFfgxPCMR";

        MvcResult mvcResult = mvc.perform(get(request,shortUrl))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.recordCount").value(0))
                .andReturn();

        Assert.assertEquals(MediaType.APPLICATION_JSON_VALUE,
                mvcResult.getResponse().getContentType());
    }

}