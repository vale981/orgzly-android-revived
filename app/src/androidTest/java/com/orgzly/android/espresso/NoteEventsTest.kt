package com.orgzly.android.espresso

import android.icu.util.Calendar
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.orgzly.R
import com.orgzly.android.OrgzlyTest
import com.orgzly.android.espresso.util.EspressoUtils.onBook
import com.orgzly.android.espresso.util.EspressoUtils.onItemInAgenda
import com.orgzly.android.espresso.util.EspressoUtils.onNoteInBook
import com.orgzly.android.espresso.util.EspressoUtils.onNoteInSearch
import com.orgzly.android.espresso.util.EspressoUtils.onNotesInAgenda
import com.orgzly.android.espresso.util.EspressoUtils.onNotesInSearch
import com.orgzly.android.espresso.util.EspressoUtils.recyclerViewItemCount
import com.orgzly.android.espresso.util.EspressoUtils.searchForTextCloseKeyboard
import com.orgzly.android.ui.main.MainActivity
import com.orgzly.org.datetime.OrgDateTime
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.junit.After
import org.junit.Test


class NoteEventsTest : OrgzlyTest() {
    private lateinit var scenario: ActivityScenario<MainActivity>

    private val now: String
            get() = OrgDateTime(true).toString()

    private val today: String
        get() = OrgDateTime.Builder()
                .setDateTime(System.currentTimeMillis())
                .setIsActive(true)
                .build()
                .toString()

    private val tomorrow: String
            get() = OrgDateTime.Builder()
                    .setDateTime(System.currentTimeMillis() + 1000 * 60 * 60 * 24)
                    .setIsActive(true)
                    .build()
                    .toString()

    private val inFewDays: String
        get() = OrgDateTime.Builder()
                .setDateTime(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3)
                .setIsActive(true)
                .build()
                .toString()

    private val yesterday: String
        get() = OrgDateTime.Builder()
                .setDateTime(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
                .setIsActive(true)
                .build()
                .toString()

    private val fewDaysAgo: String
        get() = OrgDateTime.Builder()
                .setDateTime(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3)
                .setIsActive(true)
                .build()
                .toString()

    @After
    override fun tearDown() {
        super.tearDown()
        scenario.close()
    }

    @Test
    fun search_OneInTitle() {
        testUtils.setupBook("book-a", "* Note $now")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("e.ge.today")
        onNotesInSearch().check(matches(recyclerViewItemCount(1)))
    }

    @Test
    fun search_OneInContent() {
        testUtils.setupBook("book-a", "* Note\n$now")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("e.ge.today")
        onNotesInSearch().check(matches(recyclerViewItemCount(1)))
    }

    @Test
    fun search_TwoSameInContent() {
        testUtils.setupBook("book-a", "* Note\n$now $now")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("e.ge.today")
        onNotesInSearch().check(matches(recyclerViewItemCount(1)))
    }

    @Test
    fun agenda_OneInTitle() {
        testUtils.setupBook("book-a", "* Note $now")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("ad.1")
        onNotesInAgenda().check(matches(recyclerViewItemCount(2)))
    }

    @Test
    fun agenda_TwoInTitle() {
        testUtils.setupBook("book-a", "* Note $now $tomorrow")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("ad.2")
        onNotesInAgenda().check(matches(recyclerViewItemCount(4)))
    }

    @Test
    fun agenda_OneInContent() {
        testUtils.setupBook("book-a", "* Note\n$now")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("ad.1")
        SystemClock.sleep(500)
        onNotesInAgenda().check(matches(recyclerViewItemCount(2)))
    }

    @Test
    fun agenda_TwoInContent() {
        testUtils.setupBook("book-a", "* Note\n$now $tomorrow")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("ad.2")
        onNotesInAgenda().check(matches(recyclerViewItemCount(4)))
    }

    private fun time(offset: Long = 0, hasTime: Boolean = false): OrgDateTime {
        return OrgDateTime.Builder()
                .setDateTime(System.currentTimeMillis() + offset)
                .setHasTime(hasTime)
                .setIsActive(true)
                .build()
    }

    @Test
    fun agenda_MultipleWithTimes() {
        testUtils.setupBook("book-a", """
            * Note
            SCHEDULED: ${time(1000 * 60 * 60 * 24 * 2)}
            DEADLINE: ${time(hasTime=true)}

            Now: ${time(hasTime = true)}
            In one hour: ${time(1000 * 60 * 60, hasTime = true)}
            Tomorrow: ${time(1000 * 60 * 60 * 24, hasTime = true)}"
        """.trimIndent())

        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("ad.5")

        onNotesInAgenda().check(matches(recyclerViewItemCount(10)))

        // Today: deadline
        onItemInAgenda(1, R.id.item_head_scheduled_text).check(matches(not(isDisplayed())))
        onItemInAgenda(1, R.id.item_head_deadline_text).check(matches(isDisplayed()))
        onItemInAgenda(1, R.id.item_head_event_text).check(matches(not(isDisplayed())))

        // Today: event
        onItemInAgenda(2, R.id.item_head_scheduled_text).check(matches(not(isDisplayed())))
        onItemInAgenda(2, R.id.item_head_deadline_text).check(matches(not(isDisplayed())))
        onItemInAgenda(2, R.id.item_head_event_text).check(matches(isDisplayed()))

        // Today: event
        // Item gets a different position close to midnight
        if ((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) < 23) {
            onItemInAgenda(3, R.id.item_head_scheduled_text).check(matches(not(isDisplayed())))
            onItemInAgenda(3, R.id.item_head_deadline_text).check(matches(not(isDisplayed())))
            onItemInAgenda(3, R.id.item_head_event_text).check(matches(isDisplayed()))
        } else {
            onItemInAgenda(4, R.id.item_head_scheduled_text).check(matches(not(isDisplayed())))
            onItemInAgenda(4, R.id.item_head_deadline_text).check(matches(not(isDisplayed())))
            onItemInAgenda(4, R.id.item_head_event_text).check(matches(isDisplayed()))
        }
        // Tomorrow: event
        onItemInAgenda(5, R.id.item_head_scheduled_text).check(matches(not(isDisplayed())))
        onItemInAgenda(5, R.id.item_head_deadline_text).check(matches(not(isDisplayed())))
        onItemInAgenda(5, R.id.item_head_event_text).check(matches(isDisplayed()))

        // In two days: scheduled
        onItemInAgenda(7, R.id.item_head_scheduled_text).check(matches(isDisplayed()))
        onItemInAgenda(7, R.id.item_head_deadline_text).check(matches(not(isDisplayed())))
        onItemInAgenda(7, R.id.item_head_event_text).check(matches(not(isDisplayed())))
    }

    @Test
    fun search_MultipleWithTimes() {
        testUtils.setupBook("book-a", """
            * Note
            SCHEDULED: ${time(1000 * 60 * 60 * 24 * 2)}
            DEADLINE: ${time()}

            Now: ${time(hasTime = true)}
            In one hour: ${time(1000 * 60 * 60, hasTime = true)}
            Tomorrow: ${time(1000 * 60 * 60 * 24, hasTime = true)}"
        """.trimIndent())

        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("b.book-a")

        onNotesInSearch().check(matches(recyclerViewItemCount(1)))

        onNoteInSearch(0, R.id.item_head_scheduled_text).check(matches(isDisplayed()))
        onNoteInSearch(0, R.id.item_head_deadline_text).check(matches(isDisplayed()))
        onNoteInSearch(0, R.id.item_head_event_text).check(matches(isDisplayed()))
    }

    @Test
    fun search_TodayAndInFewDays() {
        testUtils.setupBook(
                "book-a",
                "* Today $today\n* In few days $inFewDays\n* Today & In few days $today $inFewDays")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("e.gt.1d")
        onNotesInSearch().check(matches(recyclerViewItemCount(2)))
    }

    @Test
    fun agenda_PastEvent() {
        testUtils.setupBook("book-a", "* Few days ago\n$fewDaysAgo")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("ad.2")
        SystemClock.sleep(500)
        onNotesInAgenda().check(matches(recyclerViewItemCount(2)))
    }

    @Test
    fun agendaSearch_TwoWithScheduledTime() {
        testUtils.setupBook("book-a", "* $yesterday $fewDaysAgo\nSCHEDULED: $tomorrow")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("e.lt.now ad.3")
        onNotesInAgenda().check(matches(recyclerViewItemCount(4)))
    }

    @Test
    fun search_MultiplePerNote_Today() {
        testUtils.setupBook(
                "Book A",
                """
                * Note A-01
                  $today $tomorrow
                """.trimIndent())
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("e.today")
        onNotesInSearch().check(matches(recyclerViewItemCount(1)))
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(withText(startsWith("Note A-01"))))
    }

    @Test
    fun search_MultiplePerNote_OrderBy() {
        testUtils.setupBook(
                "Book A",
                """
                * Note A-01
                  <2000-01-10> <2000-01-15> <2000-01-20>
                * Note A-02
                  <2000-01-12>
                """.trimIndent())
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("e.lt.now o.e")
        onNotesInSearch().check(matches(recyclerViewItemCount(2)))
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(withText(startsWith("Note A-01"))))
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(withText(startsWith("Note A-02"))))
    }

    @Test
    fun search_MultiplePerNote_OrderByDesc() {
        testUtils.setupBook(
                "Book A",
                """
                * Note A-01
                  <2000-01-10> <2000-01-15> <2000-01-20>
                * Note A-02
                  <2000-01-12>
                """.trimIndent())
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard("e.lt.now .o.e")
        onNotesInSearch().check(matches(recyclerViewItemCount(2)))
        onNoteInSearch(0, R.id.item_head_title_view).check(matches(withText(startsWith("Note A-01"))))
        onNoteInSearch(1, R.id.item_head_title_view).check(matches(withText(startsWith("Note A-02"))))
    }

    @Test
    fun shiftFromList() {
        testUtils.setupBook("Book A", "* Note A-01 <2000-01-10 +1d>")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        onBook(0).perform(click())
        onNoteInBook(1, R.id.item_head_title_view).check(matches(withText("Note A-01 <2000-01-10 +1d>")))
        onNoteInBook(1).perform(longClick())
        onView(withId(R.id.toggle_state)).perform(click())
        onNoteInBook(1, R.id.item_head_title_view).check(matches(withText("Note A-01 <2000-01-11 Tue +1d>")))
    }

    @Test
    fun shiftFromNote() {
        testUtils.setupBook("Book A", "* Note A-01 <2000-01-10 +1d>")
        scenario = ActivityScenario.launch(MainActivity::class.java)

        onBook(0).perform(click())
        onNoteInBook(1).perform(click())
        onView(withId(R.id.title_view)).check(matches(withText("Note A-01 <2000-01-10 +1d>")))
        onView(withId(R.id.state_button)).perform(click())
        onView(withText("DONE")).perform(click())
        onView(withId(R.id.title_view)).check(matches(withText("Note A-01 <2000-01-11 Tue +1d>")))
    }

    @Test
    fun agenda_NoteWithScheduledTimeNotHiddenWhenUsingSortByEvent() {
        testUtils.setupBook(
                "Book A",
                """
                * Note A-01
                  SCHEDULED: $tomorrow
                """.trimIndent())
        scenario = ActivityScenario.launch(MainActivity::class.java)

        searchForTextCloseKeyboard(".it.done ad.7 o.e")

        onNotesInAgenda().check(matches(recyclerViewItemCount(8)))
        onItemInAgenda(2, R.id.item_head_title_view).check(matches(withText(startsWith("Note A-01"))))
    }
}
