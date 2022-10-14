package net.orfdev;

import java.util.regex.Pattern;

public interface UtilityCheck {
    static final String HTTP_HTTPS_URL_REGEX = "^((https?:\\/\\/)|(\\/)|(..\\/))(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?$";
    static final String URL_REGEX = "((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";

    static final String HOST_EXTRACTOR_REGEX = "(?:https?://)?(?:www\\.)?(.+\\.)(com|au\\.uk|co\\.in|be|in|uk|org\\.in|org|net|edu|gov|mil)";

    static final Pattern pattern = Pattern.compile("orpheussoftware", Pattern.CASE_INSENSITIVE);

    static final Pattern HOST_EXTRACTOR_REGEX_PATTERN = Pattern.compile(HOST_EXTRACTOR_REGEX);

    static final String APPLICATION_JSON_VALUE = "application/json";


    static final int NUM_MAX_TIMES = 15;
    static final int LIMIT_URL = 256;
}
