package com.huic.core.filter;

import com.huic.core.security.Encoder;

public final class URLFilter {

    static final boolean OPEN_NEW_WINDOW = true;

    private URLFilter() { //prevent instantiation
    }

    /**
     * NOTE: For security, we should call DisableHtmlTagFilter before call this method
     * @param input the string to filter
     * @return the string after being filtered
     */
    public static String filter(String input) {
        if (input == null || input.length() == 0)
            return input;
        StringBuffer buf = new StringBuffer(input.length() + 25);
        char chars[] = input.toCharArray();
        int len = input.length();
        int index = -1;
        int i = 0;
        int j = 0;
        int oldend = 0;
        while (++index < len) {
            char cur = chars[i = index];
            j = -1;
            if ((cur == 'f' && index < len - 6 && chars[++i] == 't' && chars[++i] == 'p' ||
                 cur == 'h' && (i = index) < len - 7 && chars[++i] == 't' && chars[++i] == 't' && chars[++i] == 'p' && (chars[++i] == 's' || chars[--i] == 'p'))
                 && i < len - 4 && chars[++i] == ':' && chars[++i] == '/' && chars[++i] == '/')
                j = ++i;
            if (j > 0) {// check to process http:// or https:// or ftp://
                if (index == 0 || (cur = chars[index - 1]) != '\'' && cur != '"' && cur != '<' && cur != '=') {
                    cur = chars[j];
                    while (j < len) {
                        if (cur == ' ' || cur == '\t' || cur == '\'' ||
                            cur == '"' || cur == '<' || cur == '[' ||
                            cur == '\n' ||
                            cur == '\r' && j < len - 1 && chars[j + 1] == '\n')
                            break;
                        if (++j < len)
                            cur = chars[j];
                    }
                    cur = chars[j - 1];
                    if (cur == '.' || cur == ',' || cur == ')' || cur == ']')
                        j--;
                    buf.append(chars, oldend, index - oldend);
                    buf.append("<a href=\"");
                    String href = input.substring(index, j).trim();
                    //buf.append(chars, index, j - index);
                    buf.append(Encoder.filterUrl(href));
                    buf.append('"');
                    if (OPEN_NEW_WINDOW)
                        buf.append(" target=\"_blank\"");
                    buf.append('>');
                    //buf.append(chars, index, j - index);
                    buf.append(href);// should we filter it ???
                    buf.append("</a>");
                } else {
                    buf.append(chars, oldend, j - oldend);
                }
                oldend = index = j;
            } else
            if (cur == '[' && index < len - 6 && chars[i = index + 1] == 'u' && chars[++i] == 'r' && chars[++i] == 'l' &&
                (chars[++i] == '=' || chars[i] == ' ')) {
                // process [url]
                j = ++i;
                int u2;
                int u1 = u2 = input.indexOf("]", j);
                if (u1 > 0) {
                    u2 = input.indexOf("[/url]", u1 + 1);
                }
                if (u2 < 0) {
                    buf.append(chars, oldend, j - oldend);
                    oldend = j;
                } else {
                    buf.append(chars, oldend, index - oldend);
                    buf.append("<a href=\"");
                    String href = input.substring(j, u1).trim();
                    // Add http:// to the front of links if and only if it doesn't have any protocols.
                    // Doing this handles this: "[url=sun.com]SUN[/url]"
                    // Changing it to <a href="http://sun.com">SUN</a>
                    // instead of <a href="http://localhost:8080/mvnforum/sun.com">SUN</a>
                    if ( (href.indexOf("://") == -1) && (href.startsWith("mailto:") == false) ) {
                        href = "http://" + href;
                    }
                    if (href.indexOf("javascript:") == -1 && href.indexOf("file:") == -1) {
                        buf.append(Encoder.filterUrl(href));
                    }
                    if (OPEN_NEW_WINDOW)
                        buf.append("\" target=\"_blank");
                    buf.append("\">");
                    buf.append(input.substring(u1 + 1, u2).trim());
                    buf.append("</a>");
                    oldend = u2 + 6; // 6 == length of [/url]
                }
                index = oldend - 1;// set to the last char of the tag, that is ']'
            } else
            if (cur == '[' && index < len - 6 && chars[i = index + 1] == 'i' && chars[++i] == 'm' && chars[++i] == 'g' && chars[++i] == ']' ) {
                //process [img]
                j = ++i;
                int u1 = j-1;
                int u2 = input.indexOf("[/img]", u1 + 1);
                if (u2 < 0) {
                    buf.append(chars, oldend, j - oldend);
                    oldend = j;
                } else {
                    buf.append(chars, oldend, index - oldend);
                    buf.append("<img src=\"");
                    String href = input.substring(u1 + 1, u2).trim();
                    // Add http:// to the front of links if and only if it doesn't have any protocols.
                    // Doing this handles this: "[url=sun.com]SUN[/url]"
                    // Changing it to <a href="http://sun.com">SUN</a>
                    // instead of <a href="http://localhost:8080/mvnforum/sun.com">SUN</a>
                    if (href.indexOf("://") == -1) {
                        href = "http://" + href;
                    }
                    if (href.indexOf("javascript:") == -1 && href.indexOf("file:") == -1) {
                        buf.append(Encoder.filterUrl(href));
                    }
                    buf.append("\" border=\"0\">");
                    oldend = u2 + 6; // 6 == length of [/img]
                }
                index = oldend -1;// set to the last char of the tag, that is ']'
            }
        }
        if (oldend < len)
            buf.append(chars, oldend, len - oldend);
        return buf.toString();
    }

    public static void main(String[] args) {
        //encodePath("localhost:8080/path/index.jsp");
        String[] input = {
            "[url=mailto:minhnn@myvietnam.net]Minh[/url][img]http://localhost:8080/mvnforum/mvnplugin/mvnforum/images/logo.gif[/img]",
            "-dfadg=[img] \" onmousemove=\"alert(1); [/img]",
            "(= http://a\"onmouseover='alert(1);')",
            "[url=http://sun.com]SUN[/url] http://sun.com",
            "[url sun.com]SUN[/url]", //What to do if no http???
            "[url=javascript:alert(1);]SUN[/url]",
            "[url=\" onmousemove=\"alert(1);]Hack[/url]",
            "[url=\" onmousemove='alert(1);']Hack[/url]"//somebody wants to hack us
        };

        //URLFilter enableMVNCodeFilter = new URLFilter();
        long start = System.currentTimeMillis();

        for (int i = 0; i < input.length; i++) {
            System.out.println("input = '" + input[i] + "' length = " + input[i].length());

            String output = null;
            for (int j = 0; j < 1; j++) {
                output = URLFilter.filter(input[i]);
            }

            System.out.println("output= '" + output + "'");
        }

        long time = System.currentTimeMillis() - start;
        System.out.println("total time = " + time);
    }

    /*
    public static String enableImg(String input) {
        String output = input;
        try {
            RE exp = new RE("(.*)\\[img\\](.*)\\[\\/img\\](.*)");
            boolean matched = exp.match(input);
            if (matched) {
                String front = new String();
                String back = new String();
                String matchedPattern = new String();

                front = exp.getParen(1);
                matchedPattern = exp.getParen(2);
                matchedPattern = "<img src=\"" + matchedPattern + "\" border=0 >";
                back = exp.getParen(3);

                output = front + matchedPattern + back;

                //log.info("image path is: " + output);
            }
        } catch (RESyntaxException e) {
            //log.info(e.getMessage());
        }
        return output;
    }*/

    /*
    public static void main1(String[] args) {
        URLFilter enableMVNCodeFilter = new URLFilter();
        long start = System.currentTimeMillis();

        String input = "[img]http://loclahost/test[/img] [img]http://[/img]";
        System.out.println("input = '" + input + "' length = " + input.length());

        String output = null;
        for (int j = 0; j < 1; j++) {
            output = enableMVNCodeFilter.enableImg(input);
        }

        System.out.println("output= '" + output + "'");

        long time = System.currentTimeMillis() - start;
        System.out.println("total time = " + time);
    }
    */
}
