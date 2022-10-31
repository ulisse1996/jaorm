package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.dsl.query.QueryBuilder;
import io.github.ulisse1996.jaorm.integration.test.entity.User;
import io.github.ulisse1996.jaorm.integration.test.entity.UserColumns;
import io.github.ulisse1996.jaorm.integration.test.projection.*;
import io.github.ulisse1996.jaorm.integration.test.query.UserDAO;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import org.assertj.core.util.TriFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@SuppressWarnings("java:S100")
public abstract class VendorFunctionsIT extends AbstractIT {

    private final UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

    @Test
    void should_get_coalesce_inline_value() {
        List<UsernameProjection> values = prepareCoalesce(null);

        Assertions.assertEquals(1, values.size());
        Assertions.assertEquals("OTHER", values.get(0).getName());
    }

    @Test
    void should_get_first_coalesce() {
        List<UsernameProjection> values = prepareCoalesce("NAME");

        Assertions.assertEquals(1, values.size());
        Assertions.assertEquals("NAME", values.get(0).getName());
    }

    @Test
    void should_get_upper_value() {
        User user = createUser(false);
        userDAO.insert(user);

        Optional<UsernameProjection> opt = QueryBuilder.select(AnsiFunctions.upper(UserColumns.USER_NAME).as("USER_NAME"))
                    .from(UserColumns.TABLE_NAME)
                    .readOpt(UsernameProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("NAME", opt.get().getName());
    }

    @Test
    void should_get_lower_value() {
        User user = createUser(true);
        userDAO.insert(user);

        Optional<UsernameProjection> opt = QueryBuilder.select(AnsiFunctions.lower(UserColumns.USER_NAME).as("USER_NAME"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("name", opt.get().getName());
    }

    @Test
    void should_get_only_elements_with_length_name_gt_5() {
        User user = createUser(true, "LONG_NAME");
        User user2 = createUser(true, "NAME");
        user2.setId(2);
        userDAO.insert(Arrays.asList(user, user2));

        Optional<UsernameProjection> opt = QueryBuilder.select(UserColumns.USER_NAME)
                .from(UserColumns.TABLE_NAME)
                .where(AnsiFunctions.length(UserColumns.USER_NAME)).gt(5L)
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals(user.getName(), opt.get().getName());
    }

    @Test
    void should_replace_match() {
        User user = createUser(true, "LONG_NAME");
        userDAO.insert(user);

        Optional<UsernameProjection> opt = QueryBuilder.select(AnsiFunctions.replace(UserColumns.USER_NAME, "LONG", "SHORT").as("USER_NAME"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("SHORT_NAME", opt.get().getName());
    }

    @Test
    void should_get_current_date() {
        userDAO.insert(createUser(false)); // simple record just for read
        LocalDate date = LocalDate.now()
                .minus(3, ChronoUnit.DAYS);

        Optional<DateProjection> opt = QueryBuilder.select(getCurrentDateFn().as("MY_DATE"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(DateProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertTrue(date.isBefore(opt.get().getMyDate()));
    }

    @Test
    void should_get_current_timestamp() {
        userDAO.insert(createUser(false)); // simple record just for read

        Optional<TimestampProjection> opt = QueryBuilder.select(getCurrentTimestampFn().as("MY_TIMESTAMP"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(TimestampProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertNotNull(opt.get().getMyTimestamp());
    }

    @Test
    public void should_get_current_time() {
        userDAO.insert(createUser(false)); // simple record just for read

        Optional<TimeProjection> opt = QueryBuilder.select(getTimeFn().as("MY_TIME"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(TimeProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertNotNull(opt.get());
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_return_concat_value() {
        User user = createUser(true);
        userDAO.insert(user);

        Optional<UsernameProjection> opt = QueryBuilder.select(
                getConcatFn(InlineValue.inline("F_"), UserColumns.USER_NAME, InlineValue.inline("_L")).as("USER_NAME"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("F_NAME_L", opt.get().getName());
    }

    @Test
    void should_return_sub_string() {
        User user = createUser(true, "SUPER_NAME");
        userDAO.insert(user);

        Optional<UsernameProjection> opt = QueryBuilder.select(getSubstringFn().apply(5, UserColumns.USER_NAME).as("USER_NAME"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("SUPER", opt.get().getName());
    }

    @ParameterizedTest
    @MethodSource("getTrimValues")
    void should_create_trim_value(Type type, String value) {
        User user = createUser(true, value);
        userDAO.insert(user);

        Optional<UsernameProjection> opt = QueryBuilder.select(getTrimFn().apply(type, UsernameProjectionColumns.USER_NAME).as("USER_NAME"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("SPACED", opt.get().getName());
    }

    @ParameterizedTest
    @MethodSource("getTrimValuesWithChar")
    void should_create_trim_value_using_custom_char(Type type, String value, char replaceChar) {
        User user = createUser(true, value);
        userDAO.insert(user);

        Optional<UsernameProjection> opt = QueryBuilder.select(getTrimWithCharFn()
                        .apply(type, UsernameProjectionColumns.USER_NAME, replaceChar).as("USER_NAME"))
                .from(UserColumns.TABLE_NAME)
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("SPACED", opt.get().getName());
    }

    private static Stream<Arguments> getTrimValuesWithChar() {
        return Stream.of(
                Arguments.of(Type.LEADING, "--SPACED", '-'),
                Arguments.of(Type.BOTH, "-SPACED-", '-'),
                Arguments.of(Type.TRAILING, "SPACED-", '-')
        );
    }

    private static Stream<Arguments> getTrimValues() {
        return Stream.of(
                Arguments.of(Type.LEADING, " SPACED"),
                Arguments.of(Type.BOTH, " SPACED "),
                Arguments.of(Type.TRAILING, "SPACED ")
        );
    }

    private User createUser(boolean upper) {
        return createUser(upper, "NAME");
    }

    private User createUser(boolean upper, String name) {
        User user = new User();
        user.setId(1);
        user.setName(upper ? name.toUpperCase() : name);
        return user;
    }

    private List<UsernameProjection> prepareCoalesce(String name) {
        User user = new User();
        user.setId(1);
        user.setName(name);

        userDAO.insert(user);

        return QueryBuilder.select(AnsiFunctions.coalesce(UserColumns.USER_NAME,
                        InlineValue.inline("OTHER")).as("USER_NAME"))
                .from(UserColumns.TABLE_NAME)
                .readAll(UsernameProjection.class);
    }

    protected abstract VendorFunction<Date> getCurrentDateFn();
    protected abstract VendorFunction<Timestamp> getCurrentTimestampFn();
    protected abstract VendorFunction<Time> getTimeFn();
    @SuppressWarnings("unchecked")
    protected abstract VendorFunction<String> getConcatFn(Selectable<String>... selectables);
    protected abstract BiFunction<Integer, Selectable<String>, VendorFunction<String>> getSubstringFn();
    protected abstract BiFunction<Type, Selectable<String>, VendorFunction<String>> getTrimFn();
    protected abstract TriFunction<Type, Selectable<String>, Character, VendorFunction<String>> getTrimWithCharFn();

    protected enum Type {
        LEADING,
        TRAILING,
        BOTH
    }
}
