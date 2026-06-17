package com.BookShop_Backend.configurations;

import com.BookShop_Backend.enums.PaymentStatus;
import com.BookShop_Backend.models.AuthorEntity;
import com.BookShop_Backend.models.BookEntity;
import com.BookShop_Backend.models.CartEntity;
import com.BookShop_Backend.models.CartItemEntity;
import com.BookShop_Backend.models.CategoryEntity;
import com.BookShop_Backend.models.OrderEntity;
import com.BookShop_Backend.models.OrderItemEntity;
import com.BookShop_Backend.models.PublisherEntity;
import com.BookShop_Backend.models.RefreshTokenEntity;
import com.BookShop_Backend.models.RoleEntity;
import com.BookShop_Backend.models.UserEntity;
import com.BookShop_Backend.repositories.AuthorRepository;
import com.BookShop_Backend.repositories.BookRepository;
import com.BookShop_Backend.repositories.CartItemRepository;
import com.BookShop_Backend.repositories.CartRepository;
import com.BookShop_Backend.repositories.CategoryRepository;
import com.BookShop_Backend.repositories.OrderItemRepository;
import com.BookShop_Backend.repositories.OrderRepository;
import com.BookShop_Backend.repositories.PublisherRepository;
import com.BookShop_Backend.repositories.RefreshTokenRepository;
import com.BookShop_Backend.repositories.RoleRepository;
import com.BookShop_Backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(SeedDataProperties.class)
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int BATCH_SIZE = 200;
    private static final String[] CATEGORY_PREFIXES = {
            "Van hoc", "Kinh doanh", "Tam ly", "Ky nang", "Thieu nhi", "Lich su",
            "Cong nghe", "Khoa hoc", "Giao duc", "Ngoai ngu"
    };
    private static final String[] AUTHOR_FIRST_NAMES = {
            "An", "Binh", "Chi", "Dung", "Giang", "Hanh", "Khanh", "Linh", "Minh", "Nam",
            "Ngoc", "Phong", "Quang", "Son", "Trang", "Viet"
    };
    private static final String[] AUTHOR_LAST_NAMES = {
            "Nguyen", "Tran", "Le", "Pham", "Hoang", "Vu", "Do", "Bui", "Dang", "Ngo"
    };
    private static final String[] PUBLISHER_NAMES = {
            "Tri Thuc", "Tre", "Kim Dong", "Lao Dong", "Thanh Nien",
            "Giao Duc", "Van Hoa", "Tong Hop", "Phuong Nam", "Thong Ke"
    };
    private static final String[] ADDRESS_POOL = {
            "Ha Noi", "Ho Chi Minh", "Da Nang", "Can Tho", "Hai Phong",
            "Hue", "Nha Trang", "Vung Tau", "Quang Ninh", "Binh Duong"
    };

    private final SeedDataProperties properties;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!properties.isEnabled()) {
            return;
        }

        if (properties.isClearExisting()) {
            clearExistingData();
        }

        RoleEntity adminRole = getRequiredRole(1L, RoleEntity.ADMIN);
        RoleEntity userRole = getRequiredRole(2L, RoleEntity.USER);
        List<AuthorEntity> authors = seedAuthors();
        List<CategoryEntity> categories = seedCategories();
        List<PublisherEntity> publishers = seedPublishers();
        SeededUsers seededUsers = seedUsers(adminRole, userRole);
        List<UserEntity> users = seededUsers.users();
        List<BookEntity> books = seedBooks(authors, categories, publishers);
        List<CartEntity> carts = seedCarts(users);

        int cartItemCount = seedCartItems(carts, books);
        int orderItemCount = seedOrders(users, books);
        int refreshTokenCount = seedRefreshTokens(users);

        log.info(
                "Seed completed: users={}, authors={}, categories={}, publishers={}, books={}, carts={}, cartItems={}, orders={}, orderItems={}, refreshTokens={}",
                userRepository.count(),
                authorRepository.count(),
                categoryRepository.count(),
                publisherRepository.count(),
                bookRepository.count(),
                cartRepository.count(),
                cartItemCount,
                orderRepository.count(),
                orderItemCount,
                refreshTokenCount
        );
        log.info("Latest seeded admin login: phone={}, password={}", seededUsers.firstAdminPhone(), properties.getAdminPassword());
        log.info("Latest seeded user login: phone={}, password={}", seededUsers.firstUserPhone(), properties.getUserPassword());
    }

    private void clearExistingData() {
        log.info("Clearing existing data before seeding.");
        refreshTokenRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        authorRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        publisherRepository.deleteAllInBatch();
    }

    private RoleEntity getRequiredRole(Long id, String expectedName) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Missing role id=" + id + " (" + expectedName + ")"));
        if (!expectedName.equalsIgnoreCase(role.getName())) {
            throw new IllegalStateException(
                    "Role id=" + id + " expected name " + expectedName + " but found " + role.getName()
            );
        }
        return role;
    }

    private List<AuthorEntity> seedAuthors() {
        long baseCount = authorRepository.count();
        List<AuthorEntity> authors = new ArrayList<>(properties.getAuthorCount());
        for (int i = 1; i <= properties.getAuthorCount(); i++) {
            long sequence = baseCount + i;
            AuthorEntity author = AuthorEntity.builder()
                    .name(AUTHOR_LAST_NAMES[(int) ((sequence - 1) % AUTHOR_LAST_NAMES.length)]
                            + " "
                            + AUTHOR_FIRST_NAMES[(int) ((sequence - 1) % AUTHOR_FIRST_NAMES.length)]
                            + " "
                            + String.format("%03d", sequence))
                    .build();
            authors.add(author);
        }
        return saveInBatches(authors, authorRepository::saveAll);
    }

    private List<CategoryEntity> seedCategories() {
        long baseCount = categoryRepository.count();
        List<CategoryEntity> categories = new ArrayList<>(properties.getCategoryCount());
        for (int i = 1; i <= properties.getCategoryCount(); i++) {
            long sequence = baseCount + i;
            categories.add(CategoryEntity.builder()
                    .name(CATEGORY_PREFIXES[(int) ((sequence - 1) % CATEGORY_PREFIXES.length)] + " " + String.format("%02d", sequence))
                    .build());
        }
        return saveInBatches(categories, categoryRepository::saveAll);
    }

    private List<PublisherEntity> seedPublishers() {
        long baseCount = publisherRepository.count();
        List<PublisherEntity> publishers = new ArrayList<>(properties.getPublisherCount());
        for (int i = 1; i <= properties.getPublisherCount(); i++) {
            long sequence = baseCount + i;
            publishers.add(PublisherEntity.builder()
                    .name("NXB " + PUBLISHER_NAMES[(int) ((sequence - 1) % PUBLISHER_NAMES.length)] + " " + String.format("%02d", sequence))
                    .build());
        }
        return saveInBatches(publishers, publisherRepository::saveAll);
    }

    private SeededUsers seedUsers(RoleEntity adminRole, RoleEntity userRole) {
        long adminBaseCount = userRepository.countByRole_Id(adminRole.getId());
        long userBaseCount = userRepository.countByRole_Id(userRole.getId());
        List<UserEntity> users = new ArrayList<>(properties.getAdminCount() + properties.getUserCount());
        String firstAdminPhone = null;
        String firstUserPhone = null;

        for (int i = 1; i <= properties.getAdminCount(); i++) {
            long sequence = adminBaseCount + i;
            String phoneNumber = nextAvailablePhoneNumber("090", sequence);
            UserEntity admin = UserEntity.builder()
                    .fullName("Admin " + String.format("%03d", sequence))
                    .phoneNumber(phoneNumber)
                    .address("Admin Area " + ADDRESS_POOL[(int) ((sequence - 1) % ADDRESS_POOL.length)])
                    .password(passwordEncoder.encode(properties.getAdminPassword()))
                    .active(true)
                    .dateOfBirth(randomDate(1980, 1998))
                    .facebookAccountId(0)
                    .googleAccountId(0)
                    .role(adminRole)
                    .build();
            if (firstAdminPhone == null) {
                firstAdminPhone = phoneNumber;
            }
            users.add(admin);
        }

        for (int i = 1; i <= properties.getUserCount(); i++) {
            long sequence = userBaseCount + i;
            String phoneNumber = nextAvailablePhoneNumber("091", sequence);
            UserEntity user = UserEntity.builder()
                    .fullName("Customer " + String.format("%04d", sequence))
                    .phoneNumber(phoneNumber)
                    .address(ADDRESS_POOL[(int) ((sequence - 1) % ADDRESS_POOL.length)] + " Street " + ((sequence % 80) + 1))
                    .password(passwordEncoder.encode(properties.getUserPassword()))
                    .active(true)
                    .dateOfBirth(randomDate(1975, 2004))
                    .facebookAccountId(0)
                    .googleAccountId(0)
                    .role(userRole)
                    .build();
            if (firstUserPhone == null) {
                firstUserPhone = phoneNumber;
            }
            users.add(user);
        }

        return new SeededUsers(saveInBatches(users, userRepository::saveAll), firstAdminPhone, firstUserPhone);
    }

    private List<BookEntity> seedBooks(List<AuthorEntity> authors, List<CategoryEntity> categories, List<PublisherEntity> publishers) {
        long baseCount = bookRepository.count();
        List<BookEntity> books = new ArrayList<>(properties.getBookCount());
        for (int i = 1; i <= properties.getBookCount(); i++) {
            long sequence = baseCount + i;
            CategoryEntity category = categories.get((int) ((sequence - 1) % categories.size()));
            AuthorEntity author = authors.get(ThreadLocalRandom.current().nextInt(authors.size()));
            PublisherEntity publisher = publishers.get(ThreadLocalRandom.current().nextInt(publishers.size()));

            BookEntity book = BookEntity.builder()
                    .name("Book " + String.format("%05d", sequence) + " - " + category.getName())
                    .content("Noi dung tom tat cho sach " + sequence + ", phu hop de test tim kiem, gio hang va don hang.")
                    .price(45000D + (sequence % 120) * 3500D)
                    .stock(10 + (int) (sequence % 140))
                    .imageUrl("https://picsum.photos/seed/book-" + sequence + "/400/600")
                    .author(author)
                    .category(category)
                    .publisher(publisher)
                    .build();
            books.add(book);
        }
        return saveInBatches(books, bookRepository::saveAll);
    }

    private List<CartEntity> seedCarts(List<UserEntity> users) {
        List<CartEntity> carts = new ArrayList<>(users.size());
        for (UserEntity user : users) {
            carts.add(CartEntity.builder().user(user).build());
        }
        return saveInBatches(carts, cartRepository::saveAll);
    }

    private int seedCartItems(List<CartEntity> carts, List<BookEntity> books) {
        List<CartItemEntity> batch = new ArrayList<>();
        int inserted = 0;

        for (CartEntity cart : carts) {
            int itemCount = ThreadLocalRandom.current().nextInt(1, properties.getMaxCartItemsPerCart() + 1);
            for (BookEntity book : pickRandomDistinct(books, itemCount)) {
                CartItemEntity item = CartItemEntity.builder()
                        .cart(cart)
                        .book(book)
                        .quantity(ThreadLocalRandom.current().nextInt(1, 4))
                        .build();
                batch.add(item);
            }

            if (batch.size() >= BATCH_SIZE) {
                inserted += cartItemRepository.saveAll(batch).size();
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            inserted += cartItemRepository.saveAll(batch).size();
        }
        return inserted;
    }

    private int seedOrders(List<UserEntity> users, List<BookEntity> books) {
        long baseOrderCode = 4_000_000_000L + System.currentTimeMillis();
        int insertedItems = 0;

        for (int start = 0; start < properties.getOrderCount(); start += BATCH_SIZE) {
            int currentBatchSize = Math.min(BATCH_SIZE, properties.getOrderCount() - start);
            List<OrderDraft> drafts = new ArrayList<>(currentBatchSize);
            List<OrderEntity> orderBatch = new ArrayList<>(currentBatchSize);

            for (int offset = 0; offset < currentBatchSize; offset++) {
                int sequence = start + offset + 1;
                UserEntity user = users.get(ThreadLocalRandom.current().nextInt(users.size()));
                int itemCount = ThreadLocalRandom.current().nextInt(1, properties.getMaxOrderItemsPerOrder() + 1);
                List<BookEntity> selectedBooks = pickRandomDistinct(books, itemCount);

                double totalPrice = 0D;
                List<OrderLineDraft> lines = new ArrayList<>(selectedBooks.size());
                for (BookEntity book : selectedBooks) {
                    int quantity = ThreadLocalRandom.current().nextInt(1, 4);
                    double linePrice = book.getPrice();
                    totalPrice += linePrice * quantity;
                    lines.add(new OrderLineDraft(book, quantity, linePrice));
                }

                OrderEntity order = OrderEntity.builder()
                        .orderCode(baseOrderCode + sequence)
                        .user(user)
                        .totalPrice(totalPrice)
                        .paymentStatus(randomPaymentStatus())
                        .address(user.getAddress())
                        .phoneNumber(user.getPhoneNumber())
                        .build();
                orderBatch.add(order);
                drafts.add(new OrderDraft(order, lines));
            }

            List<OrderEntity> savedOrders = orderRepository.saveAll(orderBatch);
            List<OrderItemEntity> orderItems = new ArrayList<>();
            for (int i = 0; i < savedOrders.size(); i++) {
                OrderEntity savedOrder = savedOrders.get(i);
                for (OrderLineDraft line : drafts.get(i).lines()) {
                    orderItems.add(OrderItemEntity.builder()
                            .order(savedOrder)
                            .book(line.book())
                            .quantity(line.quantity())
                            .price(line.price())
                            .build());
                }
            }
            insertedItems += orderItemRepository.saveAll(orderItems).size();
        }

        return insertedItems;
    }

    private int seedRefreshTokens(List<UserEntity> users) {
        List<RefreshTokenEntity> tokens = new ArrayList<>(properties.getRefreshTokenCount());
        for (int i = 1; i <= properties.getRefreshTokenCount(); i++) {
            UserEntity user = users.get(ThreadLocalRandom.current().nextInt(users.size()));
            tokens.add(RefreshTokenEntity.builder()
                    .token(createTokenValue(i))
                    .tokenType("REFRESH_TOKEN")
                    .expirationDate(randomFutureDate())
                    .revoked(false)
                    .expired(false)
                    .user(user)
                    .build());
        }
        return saveInBatches(tokens, refreshTokenRepository::saveAll).size();
    }

    private PaymentStatus randomPaymentStatus() {
        int chance = ThreadLocalRandom.current().nextInt(100);
        if (chance < 70) {
            return PaymentStatus.PAID;
        }
        if (chance < 90) {
            return PaymentStatus.UNPAID;
        }
        return PaymentStatus.REFUNDED;
    }

    private Date randomDate(int startYear, int endYear) {
        LocalDate localDate = LocalDate.of(
                ThreadLocalRandom.current().nextInt(startYear, endYear + 1),
                ThreadLocalRandom.current().nextInt(1, 13),
                ThreadLocalRandom.current().nextInt(1, 28)
        );
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date randomFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(7, 60));
        return Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String createTokenValue(int index) {
        String base = UUID.randomUUID().toString().replace("-", "") + index;
        return UUID.nameUUIDFromBytes(base.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "") + base;
    }

    private String nextAvailablePhoneNumber(String prefix, long startSequence) {
        long sequence = Math.max(1, startSequence);
        while (true) {
            String candidate = String.format("%s%07d", prefix, sequence);
            if (!userRepository.existsByPhoneNumber(candidate)) {
                return candidate;
            }
            sequence++;
        }
    }

    private <T> List<T> pickRandomDistinct(List<T> source, int count) {
        int limitedCount = Math.min(count, source.size());
        Set<Integer> indexes = new LinkedHashSet<>(limitedCount);
        while (indexes.size() < limitedCount) {
            indexes.add(ThreadLocalRandom.current().nextInt(source.size()));
        }

        List<T> result = new ArrayList<>(limitedCount);
        for (Integer index : indexes) {
            result.add(source.get(index));
        }
        return result;
    }

    private <T> List<T> saveInBatches(List<T> source, BatchSaver<T> batchSaver) {
        List<T> savedItems = new ArrayList<>(source.size());
        for (int start = 0; start < source.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, source.size());
            savedItems.addAll(batchSaver.save(source.subList(start, end)));
        }
        return savedItems;
    }

    @FunctionalInterface
    private interface BatchSaver<T> {
        List<T> save(List<T> items);
    }

    private record OrderDraft(OrderEntity order, List<OrderLineDraft> lines) {
    }

    private record OrderLineDraft(BookEntity book, int quantity, double price) {
    }

    private record SeededUsers(List<UserEntity> users, String firstAdminPhone, String firstUserPhone) {
    }
}
