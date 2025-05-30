package com.orgzly.android.espresso;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerActions.open;
import static androidx.test.espresso.contrib.PickerActions.setDate;
import static androidx.test.espresso.contrib.PickerActions.setTime;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static com.orgzly.android.espresso.util.EspressoUtils.contextualToolbarOverflowMenu;
import static com.orgzly.android.espresso.util.EspressoUtils.grantAlarmsAndRemindersSpecialPermission;
import static com.orgzly.android.espresso.util.EspressoUtils.onActionItemClick;
import static com.orgzly.android.espresso.util.EspressoUtils.onBook;
import static com.orgzly.android.espresso.util.EspressoUtils.onNoteInBook;
import static com.orgzly.android.espresso.util.EspressoUtils.onNoteInSearch;
import static com.orgzly.android.espresso.util.EspressoUtils.onNotesInSearch;
import static com.orgzly.android.espresso.util.EspressoUtils.recyclerViewItemCount;
import static com.orgzly.android.espresso.util.EspressoUtils.replaceTextCloseKeyboard;
import static com.orgzly.android.espresso.util.EspressoUtils.scroll;
import static com.orgzly.android.espresso.util.EspressoUtils.searchForTextCloseKeyboard;
import static com.orgzly.android.espresso.util.EspressoUtils.waitId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assume.assumeTrue;

import android.icu.util.Calendar;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;

import com.orgzly.R;
import com.orgzly.android.OrgzlyTest;
import com.orgzly.android.prefs.AppPreferences;
import com.orgzly.android.ui.main.MainActivity;
import com.orgzly.org.datetime.OrgDateTime;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class QueryFragmentTest extends OrgzlyTest {
    private ActivityScenario<MainActivity> scenario;

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (scenario != null) {
            scenario.close();
        }
    }

    private void defaultSetUp() {
        testUtils.setupBook("book-one",
                "First book used for testing\n" +
                "* Note A.\n" +
                "** [#A] Note B.\n" +
                "* TODO Note C.\n" +
                "SCHEDULED: <2014-01-01>\n" +
                "** Note D.\n" +
                "*** TODO Note E.\n" +
                "*** Same title in different notebooks.\n" +
                "*** Another note.\n" +
                "");

        testUtils.setupBook("book-two",
                "Sample book used for tests\n" +
                "* Note #1.\n" +
                "* Note #2.\n" +
                "** TODO Note #3.\n" +
                "** Note #4.\n" +
                "*** DONE Note #5.\n" +
                "CLOSED: [2014-06-03 Tue 13:34]\n" +
                "**** Note #6.\n" +
                "** Note #7.\n" +
                "* DONE Note #8.\n" +
                "CLOSED: [2014-06-03 Tue 3:34]\n" +
                "**** Note #9.\n" +
                "SCHEDULED: <2014-05-26 Mon>\n" +
                "** Note #10.\n" +
                "** Same title in different notebooks.\n" +
                "** Note #11.\n" +
                "** Note #12.\n" +
                "** Note #13.\n" +
                "DEADLINE: <2014-05-26 Mon>\n" +
                "** Note #14.\n" +
                "** [#A] Note #15.\n" +
                "** [#A] Note #16.\n" +
                "** [#B] Note #17.\n" +
                "** [#C] Note #18.\n" +
                "** Note #19.\n" +
                "** Note #20.\n" +
                "** Note #21.\n" +
                "** Note #22.\n" +
                "** Note #23.\n" +
                "** Note #24.\n" +
                "** Note #25.\n" +
                "** Note #26.\n" +
                "** Note #27.\n" +
                "** Note #28.\n" +
                "");

        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @Test
    public void testSearchFromBookOneResult() {
        defaultSetUp();

        onView(allOf(withText("book-one"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("b.book-one another note");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(1)));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText("Another note."), isDisplayed())));
    }

    @Test
    public void testSearchFromBookMultipleResults() {
        defaultSetUp();

        onView(allOf(withText("book-one"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("b.book-one note");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(7)));
    }

    @Test
    public void testSearchTwice() {
        defaultSetUp();

        searchForTextCloseKeyboard("different");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        searchForTextCloseKeyboard("another");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(1)));
    }

    @Test
    public void testSearchExpressionTodo() {
        defaultSetUp();

        searchForTextCloseKeyboard("i.todo");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(3)));
    }

    @Test
    public void testSearchExpressionsToday() {
        defaultSetUp();

        searchForTextCloseKeyboard("s.today");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
    }

    @Test
    public void testSearchExpressionsPriority() {
        defaultSetUp();

        searchForTextCloseKeyboard("p.a");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));

        onNotesInSearch().check(matches(recyclerViewItemCount(3)));

        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText("#A  Note B."), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText("#A  Note #15."), isDisplayed())));
        onNoteInSearch(2, R.id.item_head_title_view).check(matches(allOf(withText("#A  Note #16."), isDisplayed())));
    }

    @Test
    public void testNotPriority() {
        defaultSetUp();

        searchForTextCloseKeyboard(".p.b");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));

        onNotesInSearch().check(matches(recyclerViewItemCount(4)));

        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText("#A  Note B."), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText("#A  Note #15."), isDisplayed())));
        onNoteInSearch(2, R.id.item_head_title_view).check(matches(allOf(withText("#A  Note #16."), isDisplayed())));
        onNoteInSearch(3, R.id.item_head_title_view).check(matches(allOf(withText("#C  Note #18."), isDisplayed())));
    }


    @Test
    public void testSearchInBook() {
        defaultSetUp();

        searchForTextCloseKeyboard("b.book-one note");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(7)));
    }

    /**
     * Starting with 3 displayed to-do notes, removing state from one, expecting 2 to-do notes left.
     */
    @Test
    public void testEditChangeState() {
        defaultSetUp();

        onView(withId(R.id.drawer_layout)).perform(open());
        onView(withText("To Do")).perform(click());
        onNotesInSearch().check(matches(recyclerViewItemCount(3)));
        onView(allOf(withText(endsWith("Note C.")), isDisplayed())).perform(longClick());
        onView(withId(R.id.state)).perform(click());
        onView(withText(R.string.clear)).perform(click());
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
    }

    @Test
    public void testToggleState() {
        testUtils.setupBook("book-one", "* Note");
        scenario = ActivityScenario.launch(MainActivity.class);

        searchForTextCloseKeyboard("Note");
        onNoteInSearch(0).perform(longClick());
        onView(withId(R.id.toggle_state)).perform(click());
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(withText(startsWith("DONE"))));
    }

    /**
     * Clicks on the last note and expects it opened.
     */
    @Test
    public void testClickingNote() {
        defaultSetUp();

        onView(allOf(withText("book-two"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("b.book-two Note");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(29)));
        onNoteInSearch(27).perform(click());
        onView(withId(R.id.view_flipper)).check(matches(isDisplayed()));
        onView(allOf(withText("Note #28."), isDisplayed())).check(matches(isDisplayed()));
    }

    @Test
    public void testSchedulingNote() {
        defaultSetUp();
        grantAlarmsAndRemindersSpecialPermission();

        onView(withId(R.id.drawer_layout)).perform(open());
        onView(withText("Scheduled")).perform(click());
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));

        onView(allOf(withText(endsWith("Note C.")), isDisplayed())).perform(longClick());
        onView(withId(R.id.schedule)).perform(click());
        onView(withId(R.id.date_picker_button)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(2014, 4, 1));
        onView(withText(android.R.string.ok)).perform(click());
        SystemClock.sleep(500);
        onView(isRoot()).perform(waitId(R.id.time_picker_button, 5000));
        onView(withId(R.id.time_picker_button)).perform(scroll(), click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(9, 15));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withText(R.string.set)).perform(click());

        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        onView(withText(userDateTime("<2014-04-01 Tue 09:15>"))).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchExpressionsDefaultPriority() {
        testUtils.setupBook("book-one",
                "* Note A.\n" +
                "** [#A] Note B.\n" +
                "* TODO [#B] Note C.\n" +
                "SCHEDULED: <2014-01-01>\n" +
                "** [#C] Note D.\n" +
                "*** TODO Note E.");
        testUtils.setupBook("book-two", "* Note #1.\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        searchForTextCloseKeyboard("p.b");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));

        onNotesInSearch().check(matches(recyclerViewItemCount(4)));
    }

    @Test
    public void testMultipleNotState() {
        testUtils.setupBook("notebook-1",
                "* Note A.\n" +
                "** [#A] Note B.\n" +
                "* TODO Note C.\n" +
                "SCHEDULED: <2014-01-01>\n" +
                "** Note D.\n" +
                "");
        testUtils.setupBook("notebook-2",
                "* Note #1.\n" +
                "** TODO Note #3.\n" +
                "** Note #4.\n" +
                "*** DONE Note #5.\n" +
                "CLOSED: [2014-06-03 Tue 13:34]\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        searchForTextCloseKeyboard(".i.todo .i.done");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(5)));
    }

    /**
     * Added after a bug when using insertWithOnConflict for timestamps,
     * due to https://code.google.com/p/android/issues/detail?id=13045
     */
    @Test
    public void testNotesWithSameScheduledTimeString() throws IOException {
        testUtils.setupBook("notebook-1", "* Note A\nSCHEDULED: <2014-01-01>");
        testUtils.setupBook("notebook-2", "* Note B\nSCHEDULED: <2014-01-01>");
        scenario = ActivityScenario.launch(MainActivity.class);

        searchForTextCloseKeyboard("s.today");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
    }

    @Test
    public void testNotesWithSameDeadlineTimeString() throws IOException {
        testUtils.setupBook("notebook-1", "* Note A\nDEADLINE: <2014-01-01>");
        testUtils.setupBook("notebook-2", "* Note B\nDEADLINE: <2014-01-01>");
        scenario = ActivityScenario.launch(MainActivity.class);

        searchForTextCloseKeyboard("d.today");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
    }

    @Test
    public void testClosedTimeSearch() {
        testUtils.setupBook("notebook-1", "* Note A\nCLOSED: [2014-01-01]");
        scenario = ActivityScenario.launch(MainActivity.class);

        searchForTextCloseKeyboard("c.ge.-2d");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onView(withText(R.string.no_notes_found_after_search)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(0)));
    }

    @Test
    public void testInheritedTagSearchWhenMultipleAncestorsMatch() {
        testUtils.setupBook("notebook-1",
                "* Note A :tagtag:\n" +
                "** Note B :tag:\n" +
                "*** Note C\n" +
                "*** Note D\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook-1"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("t.tag");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(4)));
    }

    @Test
    public void testInheritedAndOwnTag() {
        testUtils.setupBook("notebook-1",
                "* Note A :tag1:\n" +
                "** Note B :tag2:\n" +
                "*** Note C\n" +
                "*** Note D\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook-1"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("t.tag1 t.tag2");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(3)));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText(startsWith("Note B")), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText(startsWith("Note C")), isDisplayed())));
        onNoteInSearch(2, R.id.item_head_title_view).check(matches(allOf(withText(startsWith("Note D")), isDisplayed())));
    }

    @Test
    public void testInheritedTagsAfterMovingNote() {
        testUtils.setupBook("notebook-1",
                "* Note A :tag1:\n" +
                "** Note B :tag2:\n" +
                "*** Note C :tag3:\n" +
                "*** Note D :tag3:\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook-1"), isDisplayed())).perform(click());

        /* Move Note C down. */
        onNoteInBook(3).perform(longClick());
        onActionItemClick(R.id.move, R.string.move);
        onView(withId(R.id.notes_action_move_down)).perform(click());
        pressBack();
        pressBack();

        searchForTextCloseKeyboard("t.tag3");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        onNoteInSearch(0, R.id.item_head_title_view)
                .check(matches(allOf(withText("Note D  tag3 • tag2 tag1"), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view)
                .check(matches(allOf(withText("Note C  tag3 • tag2 tag1"), isDisplayed())));
    }

    @Test
    public void testInheritedTagsAfterDemotingSubtree() {
        testUtils.setupBook("notebook-1",
                "* Note A :tag1:\n" +
                "* Note B :tag2:\n" + // Demote
                "** Note C :tag3:\n" +
                "** Note D :tag3:\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook-1"), isDisplayed())).perform(click());

        /* Demote Note B. */
        onNoteInBook(2).perform(longClick());
        onActionItemClick(R.id.move, R.string.move);
        onView(withId(R.id.notes_action_move_right)).perform(click());
        pressBack();
        pressBack();

        searchForTextCloseKeyboard("t.tag3");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        onNoteInSearch(0, R.id.item_head_title_view)
                .check(matches(allOf(withText("Note C  tag3 • tag1 tag2"), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view)
                .check(matches(allOf(withText("Note D  tag3 • tag1 tag2"), isDisplayed())));
    }

    @Test
    public void testInheritedTagsAfterCutAndPasting() {
        testUtils.setupBook("notebook-1",
                "* Note A :tag1:\n" +
                "* Note B :tag2:\n" +
                "** Note C :tag3:\n" +
                "** Note D :tag3:\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook-1"), isDisplayed())).perform(click());

        /* Cut Note B. */
        onNoteInBook(2).perform(longClick());

        onActionItemClick(R.id.cut, R.string.cut);

        /* Paste under Note A. */
        onNoteInBook(1).perform(longClick());
        onActionItemClick(R.id.paste, R.string.paste);
        onView(withText(R.string.heads_action_menu_item_paste_under)).perform(click());

        searchForTextCloseKeyboard("t.tag3");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        onNoteInSearch(0, R.id.item_head_title_view)
                .check(matches(allOf(withText("Note C  tag3 • tag1 tag2"), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view)
                .check(matches(allOf(withText("Note D  tag3 • tag1 tag2"), isDisplayed())));
    }

    @Test
    public void testSearchOrderScheduled() {
        testUtils.setupBook("notebook-1",
                "* Note A\n" +
                "SCHEDULED: <2014-02-01>\n" +
                "** Note B\n" +
                "SCHEDULED: <2014-01-01>\n" +
                "*** Note C\n" +
                "*** Note D\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook-1"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("note o.scheduled");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(withText("Note B")));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(withText("Note A")));
    }

    @Test
    public void testOrderScheduledWithAndWithoutTimePart() {
        testUtils.setupBook("notebook-1",
                "* Note A\n" +
                "SCHEDULED: <2014-01-01>\n" +
                "** Note B\n" +
                "SCHEDULED: <2014-01-02>\n" +
                "*** Note C\n" +
                "SCHEDULED: <2014-01-02 10:00>\n" +
                "*** DONE Note D\n" +
                "SCHEDULED: <2014-01-03>\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook-1"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("s.today .i.done o.s");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(withText("Note A")));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(withText("Note C")));
        onNoteInSearch(2, R.id.item_head_title_view).check(matches(withText("Note B")));
    }

    @Test
    public void testOrderDeadlineWithAndWithoutTimePartDesc() {
        testUtils.setupBook("notebook-1",
                "* Note A\n" +
                "DEADLINE: <2014-01-01>\n" +
                "** Note B\n" +
                "DEADLINE: <2014-01-02>\n" +
                "*** Note C\n" +
                "DEADLINE: <2014-01-02 10:00>\n" +
                "*** DONE Note D\n" +
                "DEADLINE: <2014-01-03>\n" +
                "");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook-1"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("d.today .i.done .o.d");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(withText("Note B")));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(withText("Note C")));
        onNoteInSearch(2, R.id.item_head_title_view).check(matches(withText("Note A")));
    }

    @Test
    public void testOrderOfBooksAfterRenaming() {
        defaultSetUp();

        searchForTextCloseKeyboard("note");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNoteInSearch(0, R.id.item_head_book_name_text).check(matches(withText("book-one")));

        onView(withId(R.id.drawer_layout)).perform(open());
        onView(withText(R.string.notebooks)).perform(click());

        onBook(0).perform(longClick());
        contextualToolbarOverflowMenu().perform(click());
        onView(withText(R.string.rename)).perform(click());
        onView(withId(R.id.name)).perform(replaceTextCloseKeyboard("renamed book-one"));
        onView(withText(R.string.rename)).perform(click());

        /* The other book is now first. Rename it too to keep the order of notes the same. */
        onBook(0).perform(longClick());
        contextualToolbarOverflowMenu().perform(click());
        onView(withText(R.string.rename)).perform(click());
        onView(withId(R.id.name)).perform(replaceTextCloseKeyboard("renamed book-two"));
        onView(withText(R.string.rename)).perform(click());

        pressBack();

        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNoteInSearch(0, R.id.item_head_book_name_text).check(matches(withText("renamed book-one")));
    }

    @Test
    public void testSearchForNonExistentTagShouldReturnAllNotes() {
        testUtils.setupBook("notebook",
                "* Note A :a:\n" +
                "** Note B :b:\n" +
                "*** Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard(".t.c");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(4)));
    }

    @Test
    public void testNotTagShouldReturnSomeNotes() {
        testUtils.setupBook("notebook",
                "* Note A :a:\n" +
                "** Note B :b:\n" +
                "*** Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        SystemClock.sleep(500);
        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard(".t.b");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText("Note A  a"), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText("Note D"), isDisplayed())));
    }

    @Test
    public void testSearchForTagOrTag() {
        testUtils.setupBook("notebook",
                "* Note A :a:\n" +
                "** Note B :b:\n" +
                "*** Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("tn.a or tn.b");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText(startsWith("Note A")), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText(startsWith("Note B")), isDisplayed())));
    }

    @Test
    public void testSortByPriority() {
        testUtils.setupBook("notebook",
                "* [#B] Note A :a:\n" +
                "** [#A] Note B :b:\n" +
                "*** [#C] Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("o.p");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note B")), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note A")), isDisplayed())));
        onNoteInSearch(2, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note D")), isDisplayed())));
        onNoteInSearch(3, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note C")), isDisplayed())));
    }

    @Test
    public void testSortByPriorityDesc() {
        testUtils.setupBook("notebook",
                "* [#B] Note A :a:\n" +
                "** [#A] Note B :b:\n" +
                "*** [#C] Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        SystemClock.sleep(200);
        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard(".o.p");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note C")), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note D")), isDisplayed())));
        onNoteInSearch(2, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note A")), isDisplayed())));
        onNoteInSearch(3, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note B")), isDisplayed())));
    }

    @Test
    public void testSearchNoteStateType() {
        AppPreferences.states(context, "TODO NEXT | DONE");
        testUtils.setupBook("notebook",
                "* TODO Note A :a:\n" +
                "** NEXT Note B :b:\n" +
                "* DONE Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard(".it.todo");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note C")), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note D")), isDisplayed())));
    }

    @Test
    public void testSearchStateType() {
        AppPreferences.states(context, "TODO NEXT | DONE");
        testUtils.setupBook("notebook",
                "* TODO Note A :a:\n" +
                "** NEXT Note B :b:\n" +
                "* DONE Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("it.todo");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(2)));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note A")), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note B")), isDisplayed())));
    }

    @Test
    public void testSearchNoState() {
        AppPreferences.states(context, "TODO NEXT | DONE");
        testUtils.setupBook("notebook",
                "* TODO Note A :a:\n" +
                "** NEXT Note B :b:\n" +
                "* DONE Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard("it.none");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(1)));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note D")), isDisplayed())));
    }

    @Test
    public void testSearchWithState() {
        AppPreferences.states(context, "TODO NEXT | DONE");
        testUtils.setupBook("notebook",
                "* TODO Note A :a:\n" +
                "** NEXT Note B :b:\n" +
                "* DONE Note C\n" +
                "* Note D\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        searchForTextCloseKeyboard(".it.none");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(3)));
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note A")), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note B")), isDisplayed())));
        onNoteInSearch(2, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note C")), isDisplayed())));
    }

    @Test
    public void testContentOfFoldedNoteDisplayed() {
        AppPreferences.isNotesContentDisplayedInSearch(context, true);
        testUtils.setupBook("notebook",
                "* Note A\n" +
                "** Note B\n" +
                "Content for Note B\n" +
                "* Note C\n");
        scenario = ActivityScenario.launch(MainActivity.class);

        onView(allOf(withText("notebook"), isDisplayed())).perform(click());
        onNoteInBook(1, R.id.item_head_fold_button).perform(click());
        searchForTextCloseKeyboard("note");
        onView(withId(R.id.fragment_query_search_view_flipper)).check(matches(isDisplayed()));
        onNotesInSearch().check(matches(recyclerViewItemCount(3)));
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(allOf(withText(containsString("Note B")), isDisplayed())));
        onNoteInSearch(1, R.id.item_head_content_view).check(matches(allOf(withText(containsString("Content for Note B")), isDisplayed())));
    }

    @Test
    public void testDeSelectRemovedNoteInSearch() {
        testUtils.setupBook("notebook", "* TODO Note A\n* TODO Note B");
        scenario = ActivityScenario.launch(MainActivity.class);

        searchForTextCloseKeyboard("i.todo");

        onNoteInSearch(0).perform(longClick());

        onNotesInSearch().check(matches(recyclerViewItemCount(2)));

        // Check title for number of selected notes
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.top_toolbar))))
                .check(matches(withText("1")));

        // Remove state from selected note
        onView(withId(R.id.state)).perform(click());
        onView(withText(R.string.clear)).perform(click());

        onNotesInSearch().check(matches(recyclerViewItemCount(1)));

        // Check subtitle for search query
        onView(allOf(instanceOf(TextView.class), not(withText(R.string.search)), withParent(withId(R.id.top_toolbar))))
                .check(matches(withText("i.todo")));
    }

    @Test
    public void testNoNotesFoundMessageIsDisplayedInSearch() {
        scenario = ActivityScenario.launch(MainActivity.class);
        searchForTextCloseKeyboard("Note");
        SystemClock.sleep(200);
        onView(withText(R.string.no_notes_found_after_search)).check(matches(isDisplayed()));
    }

    @Ignore("Not implemented yet")
    @Test
    public void testPreselectedStateOfSelectedNote() {
        testUtils.setupBook("notebook", "* TODO Note A\n* TODO Note B");
        scenario = ActivityScenario.launch(MainActivity.class);

        searchForTextCloseKeyboard("i.todo");

        onNoteInSearch(1).perform(longClick());

        onView(withId(R.id.state)).perform(click());

        onView(withText("TODO")).check(matches(isChecked()));
    }

    @Test
    public void testSearchAndClickOnNoteWithTwoDifferentEvents() {
        testUtils.setupBook("notebook", "* Note\n<2000-01-01>\n<2000-01-02>");
        scenario = ActivityScenario.launch(MainActivity.class);
        searchForTextCloseKeyboard("e.lt.now");
        onNoteInSearch(0).perform(click());
    }

    @Test
    public void testInactiveScheduled() {
        testUtils.setupBook("notebook-1", "* Note A\nSCHEDULED: [2020-07-01]");
        scenario = ActivityScenario.launch(MainActivity.class);
        searchForTextCloseKeyboard("s.le.today");
        onNotesInSearch().check(matches(recyclerViewItemCount(0)));
    }

    @Test
    public void testInactiveDeadline() {
        testUtils.setupBook("notebook-1", "* Note A\nDEADLINE: [2020-07-01]");
        scenario = ActivityScenario.launch(MainActivity.class);
        searchForTextCloseKeyboard("d.le.today");
        onNotesInSearch().check(matches(recyclerViewItemCount(0)));
    }

    @Test
    public void testScheduledTimestamp() {
        Calendar calendar = Calendar.getInstance();
        // Skip this test if current time is less than an hour before midnight
        assumeTrue(calendar.get(Calendar.HOUR_OF_DAY) < 23);
        final long currentTime = calendar.getTimeInMillis();
        String inOneHour = new OrgDateTime.Builder()
                .setDateTime(currentTime + 1000 * 60 * 60)
                .setHasTime(true)
                .setIsActive(true)
                .build()
                .toString();

        testUtils.setupBook("notebook-1", "* Note A\nSCHEDULED: " + inOneHour);

        scenario = ActivityScenario.launch(MainActivity.class);

        onBook(0).perform(click());

        // Remove time usage
        onView(allOf(withText(endsWith("Note A")), isDisplayed())).perform(longClick());
        onView(withId(R.id.schedule)).perform(click());
        onView(withId(R.id.time_used_checkbox)).perform(scroll(), click());
        onView(withText(R.string.set)).perform(click());
        pressBack();

        searchForTextCloseKeyboard("s.now");

        onNotesInSearch().check(matches(recyclerViewItemCount(1)));
    }

    @Test
    public void testNotScheduled() {
        testUtils.setupBook("notebook-1", "* Note A");
        scenario = ActivityScenario.launch(MainActivity.class);
        searchForTextCloseKeyboard("s.no");
        onNotesInSearch().check(matches(recyclerViewItemCount(1)));
    }
}
