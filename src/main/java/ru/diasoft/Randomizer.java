package ru.diasoft;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Генерирует случайные значения требуемых типов
 */
public class Randomizer {
    static Random rnd = new Random();

    public static String rndUUID() {
        return UUID.randomUUID().toString();
    }

    public static boolean rndBoolean() {
        return rnd.nextBoolean();
    }

    public static int rndInt(int bound) {
        return rnd.nextInt(bound);
    }

    public static BigInteger rndBigInt(int min, int bound) {
        return BigInteger.valueOf(min + rndInt(bound));
    }

    public static long rndLong(int min, int bound) {
        return (min + rndInt(bound));
    }

    public static float rndFloat(float multiplier) {
        return rnd.nextFloat() * multiplier;
    }

    public static double rndDouble(double multiplier) {
        return rnd.nextDouble() * multiplier;
    }

    public static String rndName() {
        return statesOfUSA.get(rnd.nextInt(statesOfUSA.size()));
    }

    public static String rndString(int lengthBound) {
        String availableSymbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-";

        int length = rnd.nextInt(lengthBound) + 5;
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = availableSymbols.charAt(rnd.nextInt(availableSymbols.length()));
        }
        return new String(text);
    }

    public static String rndPostCode() {
        String availableSymbols = "1234567890";

        int length = 6;
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = availableSymbols.charAt(rnd.nextInt(availableSymbols.length()));
        }
        return new String(text);
    }

    public static String rndNumericString(int lengthBound) {
        String availableSymbols = "1234567890";

        int length = rnd.nextInt(lengthBound) + 1;
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = availableSymbols.charAt(rnd.nextInt(availableSymbols.length()));
        }
        return new String(text);
    }

    public static <T> T rndListValue(List<T> list) {
        return list.get(rndInt(list.size()));
    }

    public static <T extends Enum<?>> T rndEnum(Class<T> clazz) {
        int x = rnd.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    public static XMLGregorianCalendar rndXMLGregorianCalendar() throws DatatypeConfigurationException {
        Date date = rndDate();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    }

    public static Date rndDate() {
        long startMillis = 1514754000000L; //2018-01-01T00:00:00
        long endMillis = 1654030800000L; //2022-06-01T00:00:00
        long randomMillisSinceEpoch = ThreadLocalRandom
                .current()
                .nextLong(startMillis, endMillis);
        Date date = new Date(randomMillisSinceEpoch);
        return date;
    }

//    public static XMLGregorianCalendar getNextDate(XMLGregorianCalendar xmlGregorianCalendar) throws DatatypeConfigurationException {
//        DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlGregorianCalendar);
//        Duration duration = DatatypeFactory.newInstance().newDuration(2505600000L); //30 дней
//        xmlGregorianCalendar.add(duration);
//        return
//    }

    static List<String> statesOfUSA = Arrays.asList("Alabama",
            "Alaska",
            "Arizona",
            "Arkansas",
            "California",
            "Colorado",
            "Connecticut",
            "Delaware",
            "Florida",
            "Georgia",
            "Hawaii",
            "Idaho",
            "Illinois",
            "Indiana",
            "Iowa",
            "Kansas",
            "Kentucky",
            "Louisiana",
            "Maine",
            "Maryland",
            "Massachusetts",
            "Michigan",
            "Minnesota",
            "Mississippi",
            "Missouri",
            "Montana",
            "Nebraska",
            "Nevada",
            "New Hampshire",
            "New Jersey",
            "New Mexico",
            "New York",
            "North Carolina",
            "North Dakota",
            "Ohio",
            "Oklahoma",
            "Oregon",
            "Pennsylvania",
            "Rhode Island",
            "South Carolina",
            "South Dakota",
            "Tennessee",
            "Texas",
            "Utah",
            "Vermont",
            "Virginia",
            "Washington",
            "West Virginia",
            "Wisconsin",
            "Wyoming"
    );
}
