package jp.pepper_atelier_akihabara.qisdk_wrapper.value;

import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;

public class QLLanguage {
    public enum Language{
        JAPANESE,
        CHINESE,
        FRENCH,
        ENGLISH
    }

    public static Locale makeLocale(Language language){
        Locale result = null;
        switch (language){
            case JAPANESE:
                result = new Locale(com.aldebaran.qi.sdk.object.locale.Language.JAPANESE, Region.JAPAN);
                break;
            case CHINESE:
                result = new Locale( com.aldebaran.qi.sdk.object.locale.Language.CHINESE, Region.CHINA);
                break;
            case FRENCH:
                result = new Locale(com.aldebaran.qi.sdk.object.locale.Language.FRENCH, Region.FRANCE);
                break;
            case ENGLISH:
                result = new Locale(com.aldebaran.qi.sdk.object.locale.Language.ENGLISH, Region.UNITED_STATES);
                break;
        }
        return result;
    }
}
