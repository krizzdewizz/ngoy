package ngoy.common;

import ngoy.ANgoyTest;
import ngoy.Ngoy.Builder;
import ngoy.core.Component;
import ngoy.core.LocaleProvider;
import ngoy.core.NgoyException;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class DatePipeTest extends ANgoyTest {

    @Component(selector = "", template = "{{ java.time.LocalDateTime.of(2018, 10, 28, 12, 44) | date:'MMMM' }}")
    public static class DateCmp {
    }

    @Test
    public void testDefault() {
        Locale prevLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            assertThat(render(DateCmp.class)).isEqualTo("October");
        } finally {
            Locale.setDefault(prevLocale);
        }
    }

    @Test
    public void testGerman() {
        assertThat(render(DateCmp.class, useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.FRENCH)))).isEqualTo("octobre");
    }

    //

    @Component(selector = "", template = "{{ java.time.LocalDateTime.of(2018, 10, 28, 12, 44) | date }}")
    public static class NoPatternCmp {
    }

    @Test
    public void testNoPattern() {
        assertThat(render(NoPatternCmp.class)).isEqualTo("2018-10-28T12:44:00");
    }

    //

    @Component(selector = "", template = "{{ localDateTime | date }}, {{ date | date }}, {{ dateLong | date }}")
    public static class AllDatesCmp {
        public LocalDateTime localDateTime = java.time.LocalDateTime.of(2018, 10, 28, 12, 44);
        public Date date = new GregorianCalendar(2018, 10, 28, 12, 44).getTime();
        public long dateLong = date.getTime();
    }

    @Test
    public void testAllDates() {
        DatePipe.Config config = new DatePipe.Config();
        config.defaultDatePattern = "dd.MM.yyyy HH:mm:ss";
        assertThat(render(AllDatesCmp.class, builder -> provideConfig(config, builder))).isEqualTo("2018-10-28T12:44:00, 28.11.2018 12:44:00, 28.11.2018 12:44:00");
    }

    //

    @Component(selector = "", template = "{{ localDate | date }}")
    public static class LocalDateCmp {
        public LocalDate localDate = java.time.LocalDate.of(2018, 10, 28);
    }

    @Test
    public void testLocalDate() {
        DatePipe.Config config = new DatePipe.Config();
        config.defaultTemporalPattern = "dd.MM.yyyy";
        assertThat(render(LocalDateCmp.class, builder -> provideConfig(config, builder))).isEqualTo("28.10.2018");
    }

    //

    @Component(selector = "", template = "{{ null | date }}")
    public static class NullCmp {
    }

    @Test
    public void testNull() {
        assertThat(render(NullCmp.class)).isEqualTo("");
    }

    //

    @Component(selector = "", template = "{{ localDate | date }}")
    public static class WithConfigCmp {
        public LocalDate localDate = java.time.LocalDate.of(2018, 10, 28);
    }

    @Test
    public void testWithConfig() {
        DatePipe.Config config = new DatePipe.Config();
        config.defaultTemporalPattern = "dd. MMMM yyyy";
        assertThat(render(WithConfigCmp.class, builder -> provideConfig(config, builder))).isEqualTo("28. October 2018");
    }

    private Builder<?> provideConfig(DatePipe.Config config, Builder<?> builder) {
        return builder.providers(useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.ENGLISH)), useValue(DatePipe.Config.class, config));
    }

    //

    @Component(selector = "", template = "{{ dateString | date }}")
    public static class WrongTypeCmp {
        public String dateString = "1.1.2018";
    }

    @Test
    public void testWrongType() {
        expectedEx.expect(NgoyException.class);
        expectedEx.expectMessage(containsString("DatePipe: input must be one of"));
        render(WrongTypeCmp.class);
    }

}
