package io.github.ulisse1996.jaorm.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TrackedListTest {

    @Test
    void should_create_standard_tracked_list() {
        TrackedList<Object> list = TrackedList.merge(null, new ArrayList<>());
        Assertions.assertEquals(
                Collections.emptyList(),
                list
        );
        Assertions.assertEquals(
                Collections.emptyList(),
                list.getRemovedElements()
        );
    }

    @Test
    void should_create_tacked_list_from_standard_list() {
        TrackedList<Object> list = TrackedList.merge(Collections.singletonList("1"), Collections.singletonList("2"));
        Assertions.assertEquals(
                Collections.singletonList("1"),
                list.getRemovedElements()
        );
        Assertions.assertEquals(
                Collections.singletonList("2"),
                list
        );
    }

    @Test
    void should_merge_previous_tracked_list() {
        TrackedList<Object> list = new TrackedList<>(Collections.singletonList("1"), Collections.singletonList("2"));
        TrackedList<Object> newList = TrackedList.merge(list, Collections.singletonList("3"));
        Assertions.assertEquals(
                List.of("2", "1"),
                newList.getRemovedElements()
        );
        Assertions.assertEquals(Collections.singletonList("3"), newList);
    }

    @Test
    void should_add_to_removed_for_set() {
        TrackedList<Object> list = getNew("2");
        Object removed = list.set(0, "3");

        Assertions.assertEquals("2", removed);
        Assertions.assertEquals(
                List.of("3"),
                list
        );
        Assertions.assertEquals(
                List.of("2"),
                list.getRemovedElements()
        );
    }

    @Test
    void should_add_element_to_delegate() {
        TrackedList<Object> list = getNew("1");
        list.add("2");
        Assertions.assertEquals(
                List.of("1", "2"),
                list
        );
    }

    @Test
    void should_remove_element_and_add_it_to_removed_list() {
        TrackedList<Object> list = getNew("1");
        Object removed = list.remove(0);
        Assertions.assertEquals("1", removed);
        Assertions.assertEquals(List.of(), list);
        Assertions.assertEquals(List.of("1"), list.getRemovedElements());
    }

    @Test
    void should_return_same_hash_code() {
        List<String> l = List.of("2", "3", "4");
        Assertions.assertEquals(
                l.hashCode(),
                new TrackedList<>(l).hashCode()
        );
    }

    @SuppressWarnings("SimplifiableAssertion")
    @Test
    void should_return_true_for_same_delegate_items() {
        List<String> l = List.of("2", "3", "4");
        Assertions.assertTrue(getNew("2", "3", "4").equals(l));
    }

    private TrackedList<Object> getNew(Object... elements) {
        return new TrackedList<>(List.of(elements));
    }
}