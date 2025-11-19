package ac.il.bgu.qa;
import ac.il.bgu.qa.errors.BookAlreadyBorrowedException;
import ac.il.bgu.qa.errors.BookNotBorrowedException;
import ac.il.bgu.qa.errors.BookNotFoundException;
import ac.il.bgu.qa.errors.NoReviewsFoundException;
import ac.il.bgu.qa.errors.NotificationException;
import ac.il.bgu.qa.errors.ReviewException;
import ac.il.bgu.qa.errors.ReviewServiceUnavailableException;
import ac.il.bgu.qa.errors.UserNotRegisteredException;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestLibrary {

    private static final String VALID_ISBN = "9780306406157";
    private static final String VALID_TITLE = "Clean Code";
    private static final String VALID_AUTHOR = "Robert Martin";
    private static final String VALID_USER_ID = "123456789012";
    private static final String VALID_USER_NAME = "Alice";

    private DatabaseService databaseService;
    private ReviewService reviewService;
    private Library library;

    @BeforeEach
    void setUp() {
        databaseService = mock(DatabaseService.class);
        reviewService = mock(ReviewService.class);
        library = new Library(databaseService, reviewService);
    }

    @Test
    void GivenValidBook_WhenAddBook_ThenSucceed() {
        Book book = createValidBook();
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(null);

        assertDoesNotThrow(() -> library.addBook(book));

        verify(databaseService).addBook(VALID_ISBN, book);
    }

    @Test
    void GivenBookButBookNull_WhenAddBook_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.addBook(null));
    }

    @Test
    void GivenBookButInValidISBN_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book("123", VALID_TITLE, VALID_AUTHOR);

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButTitleNull_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book(VALID_ISBN, null, VALID_AUTHOR);

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButTitleEmpty_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book(VALID_ISBN, "", VALID_AUTHOR);

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButBookIsBorrowed_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = createValidBook();
        book.borrow();

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButBookAlreadyExist_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = createValidBook();
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        verify(databaseService, never()).addBook(VALID_ISBN, book);
    }

    @Test
    void GivenBookButAuthorNameNull_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, null);

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButAuthorNameEmpty_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, "");

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButAuthorNameDoesntStartWithLetter_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, "1John Doe");

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButAuthorNameDoesntEndWithLetter_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, "John Doe1");

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButAuthorNameWithIllegalChars1_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, "John@Doe");

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenBookButAuthorNameWithIllegalChars2_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, "John--Doe");

        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
    }

    @Test
    void GivenValidUser_WhenRegisterUser_ThenSucceed() {
        User user = createValidUser();
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(null);

        assertDoesNotThrow(() -> library.registerUser(user));

        verify(databaseService).registerUser(VALID_USER_ID, user);
    }

    @Test
    void GivenUserNull_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.registerUser(null));
    }

    @Test
    void GivenUserButIdNull_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User(VALID_USER_NAME, null, mock(NotificationService.class));

        assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
    }

    @Test
    void GivenUserButIdNot12Digits_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User(VALID_USER_NAME, "123", mock(NotificationService.class));

        assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
    }

    @Test
    void GivenUserButNameNull_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User(null, VALID_USER_ID, mock(NotificationService.class));

        assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
    }

    @Test
    void GivenUserButNameEmpty_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("", VALID_USER_ID, mock(NotificationService.class));

        assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
    }

    @Test
    void GivenUserButNotificationServiceNull_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User(VALID_USER_NAME, VALID_USER_ID, null);

        assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
    }

    @Test
    void GivenUserButAlreadyExists_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User existing = createValidUser();
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(existing);

        assertThrows(IllegalArgumentException.class, () -> library.registerUser(existing));
        verify(databaseService, never()).registerUser(anyString(), existing);
    }

    @Test
    void GivenValidRequest_WhenBorrowBook_ThenSucceed() {
        Book book = createValidBook();
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(createValidUser());

        library.borrowBook(VALID_ISBN, VALID_USER_ID);

        assertTrue(book.isBorrowed());
        verify(databaseService).borrowBook(VALID_ISBN, VALID_USER_ID);
    }

    @Test
    void GivenISBNNull_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.borrowBook(null, VALID_USER_ID));
    }

    @Test
    void GivenISBNButInvalid_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.borrowBook("123", VALID_USER_ID));
    }

    @Test
    void GivenValidISBNButBookNull_WhenBorrowBook_ThenThrowBookNotFoundException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(null);

        assertThrows(BookNotFoundException.class, () -> library.borrowBook(VALID_ISBN, VALID_USER_ID));
    }

    @Test
    void GivenValidRequestButUserIdNull_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(createValidBook());

        assertThrows(IllegalArgumentException.class, () -> library.borrowBook(VALID_ISBN, null));
    }

    @Test
    void GivenUserIdButInvalidFormat_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(createValidBook());

        assertThrows(IllegalArgumentException.class, () -> library.borrowBook(VALID_ISBN, "123"));
    }

    @Test
    void GivenValidRequestButUserNotRegistered_WhenBorrowBook_ThenThrowUserNotRegisteredException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(createValidBook());
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(null);

        assertThrows(UserNotRegisteredException.class, () -> library.borrowBook(VALID_ISBN, VALID_USER_ID));
    }

    @Test
    void GivenValidRequestButBookAlreadyBorrowed_WhenBorrowBook_ThenThrowBookAlreadyBorrowedException() {
        Book book = createValidBook();
        book.borrow();
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);

        assertThrows(BookAlreadyBorrowedException.class, () -> library.borrowBook(VALID_ISBN, VALID_USER_ID));
    }

    @Test
    void GivenValidISBN_WhenReturnBook_ThenSuccess() {
        Book book = new Book(VALID_ISBN, "Clean Code", "Robert Martin");
        book.borrow();
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);

        library.returnBook(VALID_ISBN);

        assertFalse(book.isBorrowed());
        verify(databaseService).returnBook(VALID_ISBN);
    }

    @Test
    void GivenValidISBNButBookNull_WhenReturnBook_ThenThrowBookNotFoundException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(null);

        assertThrows(BookNotFoundException.class, () -> library.returnBook(VALID_ISBN));
    }

    @Test
    void GivenValidISBNButBookNotBorrowed_WhenReturnBook_ThenThrowBookNotBorrowedException() {
        Book book = new Book(VALID_ISBN, "Clean Code", "Robert Martin");
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);

        assertThrows(BookNotBorrowedException.class, () -> library.returnBook(VALID_ISBN));
    }

    @Test
    void GivenNullISBM_WhenReturnBook_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.returnBook(null));
    }

    @Test
    void GivenISBMButLengthNot13_WhenReturnBook_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.returnBook("12345678901"));
    }

    @Test
    void GivenISBMButNotOnlyDigits_WhenReturnBook_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.returnBook("97803064061AB"));
    }

    @Test
    void GivenISBMButIncorrectCheckedDigit_WhenReturnBook_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.returnBook("9780306406158"));
    }

    @Test
    void GivenValidRequest_WhenNotifyUserWithBookReviews_ThenSucceed() {
        Book book = createValidBook();
        User user = mock(User.class);
        List<String> reviews = Arrays.asList("Great read", "Must have");
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(reviews);

        library.notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(user).sendNotification(captor.capture());
        String message = captor.getValue();
        assertTrue(message.contains(book.getTitle()));
        assertTrue(message.contains(reviews.get(0)));
        verify(reviewService).close();
    }

    @Test
    void GivenISBNButInvalid_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews("123", VALID_USER_ID));
    }

    @Test
    void GivenUserIdButInvalid_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(VALID_ISBN, "123"));
    }

    @Test
    void GivenValidISBNButBookNull_WhenNotifyUserWithBookReviews_ThenThrowBookNotFoundException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(null);

        assertThrows(BookNotFoundException.class, () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID));
    }

    @Test
    void GivenValidRequestButUserNotFound_WhenNotifyUserWithBookReviews_ThenThrowUserNotRegisteredException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(createValidBook());
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(null);

        assertThrows(UserNotRegisteredException.class, () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID));
    }

    @Test
    void GivenValidRequestButReviewsNull_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        Book book = createValidBook();
        User user = mock(User.class);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(null);

        assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID));
        verify(reviewService).close();
        verify(user, never()).sendNotification(anyString());
    }

    @Test
    void GivenValidRequestButReviewsEmpty_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        Book book = createValidBook();
        User user = mock(User.class);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(Collections.emptyList());

        assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID));
        verify(reviewService).close();
    }

    @Test
    void GivenValidRequestButReviewServiceThrows_WhenNotifyUserWithBookReviews_ThenThrowReviewServiceUnavailableException() {
        Book book = createValidBook();
        User user = mock(User.class);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenThrow(new ReviewException("fail"));

        assertThrows(ReviewServiceUnavailableException.class,
                () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID));
        verify(reviewService).close();
    }

    @Test
    void GivenNotificationFailsOnce_WhenNotifyUserWithBookReviews_ThenRetryAndSucceed() {
        Book book = createValidBook();
        User user = mock(User.class);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(Collections.singletonList("Great"));
        doThrow(new NotificationException("fail"))
                .doNothing()
                .when(user).sendNotification(anyString());

        library.notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID);

        verify(user, times(2)).sendNotification(anyString());
        verify(reviewService).close();
    }

    @Test
    void GivenNotificationFailsAlways_WhenNotifyUserWithBookReviews_ThenThrowNotificationException() {
        Book book = createValidBook();
        User user = mock(User.class);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USER_ID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(Collections.singletonList("Great"));
        doThrow(new NotificationException("fail")).when(user).sendNotification(anyString());

        assertThrows(NotificationException.class, () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID));
        verify(user, times(5)).sendNotification(anyString());
        verify(reviewService).close();
    }

    @Test
    void GivenValidRequest_WhenGetBookByISBN_ThenSucceed() {
        Book book = createValidBook();
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        Library spyLibrary = spy(new Library(databaseService, reviewService));
        doNothing().when(spyLibrary).notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID);

        Book result = spyLibrary.getBookByISBN(VALID_ISBN, VALID_USER_ID);

        assertEquals(book, result);
        verify(spyLibrary).notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID);
    }

    @Test
    void GivenISBNButInvalid_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN("123", VALID_USER_ID));
    }

    @Test
    void GivenUserIdButInvalid_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(VALID_ISBN, "123"));
    }

    @Test
    void GivenValidRequestButBookNotFound_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(null);

        assertThrows(BookNotFoundException.class, () -> library.getBookByISBN(VALID_ISBN, VALID_USER_ID));
    }

    @Test
    void GivenValidRequestButBookAlreadyBorrowed_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        Book book = createValidBook();
        book.borrow();
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);

        assertThrows(BookAlreadyBorrowedException.class, () -> library.getBookByISBN(VALID_ISBN, VALID_USER_ID));
    }

    @Test
    void GivenNotificationFails_WhenGetBookByISBN_ThenReturnBookAnyway() {
        Book book = createValidBook();
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        Library spyLibrary = spy(new Library(databaseService, reviewService));
        doThrow(new NotificationException("fail")).when(spyLibrary).notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID);

        Book result = spyLibrary.getBookByISBN(VALID_ISBN, VALID_USER_ID);

        assertEquals(book, result);
        verify(spyLibrary).notifyUserWithBookReviews(VALID_ISBN, VALID_USER_ID);
    }

    private Book createValidBook() {
        return new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
   }
   
    private User createValidUser() {
        NotificationService notificationService = mock(NotificationService.class);
        return new User(VALID_USER_NAME, VALID_USER_ID, notificationService);
    }
}




