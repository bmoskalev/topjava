package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> userMealWithExcessList = new ArrayList<>();
        Map<LocalDate, List<UserMeal>> mealMap = new HashMap<>();
        Map<LocalDate, Integer> localDateIntegerMap = new HashMap<>();
        for (UserMeal userMeal : meals) {
            LocalDate localDate = userMeal.getDateTime().toLocalDate();
            LocalTime localTime = userMeal.getDateTime().toLocalTime();
            localDateIntegerMap.merge(localDate, userMeal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenHalfOpen(localTime, startTime, endTime)) {
                List<UserMeal> userMealList = mealMap.getOrDefault(localDate, new ArrayList<>());
                userMealList.add(userMeal);
                mealMap.putIfAbsent(localDate, userMealList);
            }
        }
        for (Map.Entry<LocalDate, Integer> localDateIntegerEntry : localDateIntegerMap.entrySet()) {
            LocalDate localDate = localDateIntegerEntry.getKey();
            Integer calories = localDateIntegerEntry.getValue();
            List<UserMeal> userMealList = mealMap.get(localDate);
            for (UserMeal userMeal : userMealList) {
                userMealWithExcessList.add(new UserMealWithExcess(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(), calories > caloriesPerDay));
            }
        }
        return userMealWithExcessList;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> userMealWithExcessList = new ArrayList<>();
        Map<LocalDate, List<UserMeal>> mealMap;
        Map<LocalDate, Integer> localDateIntegerMap = new HashMap<>();
        mealMap = meals.stream().peek(x -> localDateIntegerMap.merge(x.getDateTime().toLocalDate(), x.getCalories(), Integer::sum)).
                filter(x -> TimeUtil.isBetweenHalfOpen(x.getDateTime().toLocalTime(), startTime, endTime)).collect(Collectors.groupingBy((x)->x.getDateTime().toLocalDate()));
        mealMap.values().stream().flatMap(Collection::stream).forEach(x->userMealWithExcessList.
                add(new UserMealWithExcess(x.getDateTime(),x.getDescription(), x.getCalories(), localDateIntegerMap.get(x.getDateTime().toLocalDate())>caloriesPerDay)));
        return userMealWithExcessList;
    }
}
