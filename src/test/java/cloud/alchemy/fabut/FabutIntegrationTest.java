package cloud.alchemy.fabut;

import cloud.alchemy.fabut.diff.BatchDiffReport;
import cloud.alchemy.fabut.model.*;
import cloud.alchemy.fabut.model.test.Address;
import cloud.alchemy.fabut.model.test.Faculty;
import cloud.alchemy.fabut.model.test.Student;
import cloud.alchemy.fabut.model.test.Teacher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that exercise the full Fabut workflow end-to-end.
 * These tests simulate real-world usage patterns with complete CRUD operations.
 */
public class FabutIntegrationTest extends AbstractFabutTest {

    // Simulated database tables
    private final List<Object> orders = new ArrayList<>();
    private final List<Object> customers = new ArrayList<>();
    private final List<Object> products = new ArrayList<>();

    private boolean assertAfterTest = true;

    public FabutIntegrationTest() {
        super();
        // Entity types - tracked in snapshot
        entityTypes.add(EntityTierOneType.class);
        entityTypes.add(EntityTierTwoType.class);
        entityTypes.add(EntityWithList.class);

        // Complex types - for deep comparison
        complexTypes.add(TierOneType.class);
        complexTypes.add(TierTwoType.class);
        complexTypes.add(Student.class);
        complexTypes.add(Address.class);
        complexTypes.add(Faculty.class);
        complexTypes.add(Teacher.class);
        complexTypes.add(AssertableEntity.class);
    }

    @BeforeEach
    @Override
    public void before() {
        super.before();
        assertAfterTest = true;
        orders.clear();
        customers.clear();
        products.clear();
    }

    @AfterEach
    @Override
    public void after() {
        if (assertAfterTest) {
            super.after();
        }
    }

    @Override
    public List<Object> findAll(Class<?> entityClass) {
        if (entityClass == EntityTierOneType.class) return orders;
        if (entityClass == EntityTierTwoType.class) return customers;
        if (entityClass == EntityWithList.class) return products;
        return Collections.emptyList();
    }

    @Override
    public Object findById(Class<?> entityClass, Object id) {
        if (entityClass == EntityTierOneType.class) {
            return orders.stream()
                    .filter(e -> Objects.equals(((EntityTierOneType) e).getId(), id))
                    .findFirst().orElse(null);
        }
        if (entityClass == EntityTierTwoType.class) {
            return customers.stream()
                    .filter(e -> Objects.equals(((EntityTierTwoType) e).getId(), id))
                    .findFirst().orElse(null);
        }
        return null;
    }

    // ==================== CRUD Lifecycle Integration Tests ====================

    @Nested
    @DisplayName("Create Entity Scenarios")
    class CreateScenarios {

        @Test
        @DisplayName("Create single entity - full assertion flow")
        void createSingleEntity_assertAllFields() {
            takeSnapshot();

            // Simulate service creating an entity
            EntityTierOneType newOrder = new EntityTierOneType("PENDING", 1);
            orders.add(newOrder);

            // Assert the created entity with all fields
            assertObject(newOrder,
                    value("id", 1),
                    value("property", "PENDING"));
        }

        @Test
        @DisplayName("Create multiple entities - batch assertion")
        void createMultipleEntities_batchAssertion() {
            takeSnapshot();

            // Simulate bulk creation
            EntityTierOneType order1 = new EntityTierOneType("PENDING", 1);
            EntityTierOneType order2 = new EntityTierOneType("PROCESSING", 2);
            EntityTierOneType order3 = new EntityTierOneType("SHIPPED", 3);
            orders.add(order1);
            orders.add(order2);
            orders.add(order3);

            // Assert all created entities
            assertObject(order1, value("id", 1), value("property", "PENDING"));
            assertObject(order2, value("id", 2), value("property", "PROCESSING"));
            assertObject(order3, value("id", 3), value("property", "SHIPPED"));
        }

        @Test
        @DisplayName("Create entity with nested reference")
        void createEntityWithNestedReference() {
            takeSnapshot();

            EntityTierOneType order = new EntityTierOneType("PENDING", 1);
            orders.add(order);

            EntityTierTwoType customer = new EntityTierTwoType("John Doe", 10, order);
            customers.add(customer);

            // Assert parent entity
            assertObject(customer,
                    value("id", 10),
                    value("property", "John Doe"),
                    value("subProperty.id", 1),
                    value("subProperty.property", "PENDING"));
        }

    }

    @Nested
    @DisplayName("Update Entity Scenarios")
    class UpdateScenarios {

        @Test
        @DisplayName("Update single field - snapshot assertion")
        void updateSingleField() {
            // Setup: existing entity
            EntityTierOneType order = new EntityTierOneType("PENDING", 1);
            orders.add(order);
            takeSnapshot();

            // Action: update the entity
            order.setProperty("SHIPPED");

            // Assert: only specify changed field
            assertEntityWithSnapshot(order, value("property", "SHIPPED"));
        }

        @Test
        @DisplayName("Update multiple fields - snapshot assertion")
        void updateMultipleFields() {
            EntityTierTwoType customer = new EntityTierTwoType("John", 1, null);
            customers.add(customer);
            takeSnapshot();

            // Update multiple fields
            customer.setProperty("John Updated");
            EntityTierOneType newOrder = new EntityTierOneType("NEW", 100);
            orders.add(newOrder);
            customer.setSubProperty(newOrder);

            // Assert changed fields
            assertEntityWithSnapshot(customer,
                    value("property", "John Updated"),
                    value("subProperty", newOrder));

            // Assert the new order was created
            assertObject(newOrder, value("id", 100), value("property", "NEW"));
        }

        @Test
        @DisplayName("Update nested entity through parent")
        void updateNestedEntity() {
            EntityTierOneType order = new EntityTierOneType("PENDING", 1);
            orders.add(order);
            EntityTierTwoType customer = new EntityTierTwoType("John", 10, order);
            customers.add(customer);
            takeSnapshot();

            // Update the nested entity's property
            order.setProperty("COMPLETED");

            // Assert the change on the order directly
            assertEntityWithSnapshot(order, value("property", "COMPLETED"));
        }

        @Test
        @DisplayName("Update with wrong expected value - should fail")
        void updateWithWrongExpectedValue_fails() {
            assertThrows(AssertionError.class, () -> {
                EntityTierOneType order = new EntityTierOneType("PENDING", 1);
                orders.add(order);
                takeSnapshot();

                order.setProperty("SHIPPED");

                // Wrong expected value
                assertAfterTest = false;
                assertEntityWithSnapshot(order, value("property", "WRONG_VALUE"));
            });
        }
    }

    @Nested
    @DisplayName("Delete Entity Scenarios")
    class DeleteScenarios {

        @Test
        @DisplayName("Delete single entity")
        void deleteSingleEntity() {
            EntityTierOneType order = new EntityTierOneType("PENDING", 1);
            orders.add(order);
            takeSnapshot();

            // Delete the entity
            orders.remove(order);

            // Assert it was deleted
            assertEntityAsDeleted(order);
        }

        @Test
        @DisplayName("Delete multiple entities")
        void deleteMultipleEntities() {
            EntityTierOneType order1 = new EntityTierOneType("OLD", 1);
            EntityTierOneType order2 = new EntityTierOneType("OLD", 2);
            EntityTierOneType order3 = new EntityTierOneType("KEEP", 3);
            orders.add(order1);
            orders.add(order2);
            orders.add(order3);
            takeSnapshot();

            // Delete some entities
            orders.remove(order1);
            orders.remove(order2);

            // Assert deletions
            assertEntityAsDeleted(order1);
            assertEntityAsDeleted(order2);
        }

    }

    @Nested
    @DisplayName("Ignore Entity Scenarios")
    class IgnoreScenarios {

        @Test
        @DisplayName("Ignore created entity - no assertion needed")
        void ignoreCreatedEntity() {
            takeSnapshot();

            // Create an audit log that we don't care about
            EntityTierOneType auditLog = new EntityTierOneType("AUDIT_ENTRY", 999);
            orders.add(auditLog);

            // Ignore it
            ignoreEntity(auditLog);

            // No assertion needed
        }

        @Test
        @DisplayName("Mixed scenario - create, update, delete, ignore")
        void mixedCrudOperations() {
            // Initial state
            EntityTierOneType existingOrder = new EntityTierOneType("PENDING", 1);
            EntityTierOneType toDelete = new EntityTierOneType("OLD", 2);
            orders.add(existingOrder);
            orders.add(toDelete);
            takeSnapshot();

            // Create new
            EntityTierOneType newOrder = new EntityTierOneType("NEW", 3);
            orders.add(newOrder);

            // Update existing
            existingOrder.setProperty("SHIPPED");

            // Delete one
            orders.remove(toDelete);

            // Create audit log (ignore it)
            EntityTierOneType auditLog = new EntityTierOneType("AUDIT", 100);
            orders.add(auditLog);

            // Assertions
            assertObject(newOrder, value("id", 3), value("property", "NEW"));
            assertEntityWithSnapshot(existingOrder, value("property", "SHIPPED"));
            assertEntityAsDeleted(toDelete);
            ignoreEntity(auditLog);
        }
    }

    // ==================== Complex Object Graph Tests ====================

    @Nested
    @DisplayName("Deep Object Graph Assertions")
    class DeepObjectGraphTests {

        @Test
        @DisplayName("Assert complex nested object structure")
        void assertDeepNestedStructure() {
            takeSnapshot();

            // Build complex object graph
            Student student = new Student();
            student.setName("Alice");
            student.setLastName("Smith");

            Address studentAddress = new Address();
            studentAddress.setCity("Boston");
            studentAddress.setStreet("Main St");
            studentAddress.setStreetNumber("123");
            student.setAddress(studentAddress);

            Faculty faculty = new Faculty();
            faculty.setName("Engineering");

            Teacher teacher = new Teacher();
            teacher.setName("Prof. Johnson");
            Address teacherAddress = new Address();
            teacherAddress.setCity("Cambridge");
            teacherAddress.setStreet("University Ave");
            teacherAddress.setStreetNumber("500");
            teacher.setAddress(teacherAddress);
            teacher.setStudent(student);  // Circular reference

            faculty.setTeacher(teacher);
            student.setFaculty(faculty);

            // Assert deep structure
            assertObject(student,
                    value("name", "Alice"),
                    value("lastName", "Smith"),
                    value("address.city", "Boston"),
                    value("address.street", "Main St"),
                    value("address.streetNumber", "123"),
                    value("faculty.name", "Engineering"),
                    value("faculty.teacher.name", "Prof. Johnson"),
                    value("faculty.teacher.address.city", "Cambridge"),
                    value("faculty.teacher.address.street", "University Ave"),
                    value("faculty.teacher.address.streetNumber", "500"),
                    value("faculty.teacher.student", student));  // Circular ref by identity
        }

        @Test
        @DisplayName("Assert list of nested objects")
        void assertListOfNestedObjects() {
            takeSnapshot();

            List<EntityTierOneType> orderItems = Arrays.asList(
                    new EntityTierOneType("Item1", 1),
                    new EntityTierOneType("Item2", 2),
                    new EntityTierOneType("Item3", 3)
            );
            orders.addAll(orderItems);

            EntityWithList container = new EntityWithList();
            container.setId(100);
            container.setList(orderItems);
            products.add(container);

            // Assert container and track orders
            assertObject(container, value("id", 100));
            for (EntityTierOneType item : orderItems) {
                assertObject(item, value("id", item.getId()), value("property", item.getProperty()));
            }
        }
    }

    // ==================== Generated Builder Integration Tests ====================

    @Nested
    @DisplayName("Generated Assert Builder Integration")
    class GeneratedBuilderIntegration {

        @Test
        @DisplayName("Full workflow with generated builder - create scenario")
        void generatedBuilder_createWorkflow() {
            takeSnapshot();

            AssertableEntity entity = new AssertableEntity(1L, "TestProduct", 100,
                    Optional.of("A test product"), Optional.of(50));

            // Use generated builder for assertion
            AssertableEntityAssert.assertCreate(FabutIntegrationTest.this, entity)
                    .idIs(1L)
                    .nameIs("TestProduct")
                    .countIs(100)
                    .descriptionHasValue("A test product")
                    .scoreHasValue(50)
                    .verify();
        }

        @Test
        @DisplayName("Generated builder with mandatory field groups")
        void generatedBuilder_withFieldGroups() {
            takeSnapshot();

            AssertableEntity entity = new AssertableEntity(1L, "Product", 25,
                    Optional.of("Desc"), Optional.of(75));

            // Use group assertion - Key group (id, name)
            AssertableEntityAssert.assertCreateKey(FabutIntegrationTest.this, entity, 1L, "Product")
                    .countIs(25)
                    .descriptionHasValue("Desc")
                    .scoreHasValue(75)
                    .verify();
        }

        @Test
        @DisplayName("Generated builder with optional fields")
        void generatedBuilder_optionalFields() {
            takeSnapshot();

            AssertableEntity entity = new AssertableEntity(1L, "MinimalProduct", 10,
                    Optional.empty(), Optional.empty());

            AssertableEntityAssert.assertCreate(FabutIntegrationTest.this, entity)
                    .idIs(1L)
                    .nameIs("MinimalProduct")
                    .countIs(10)
                    .descriptionIsEmpty()
                    .scoreIsEmpty()
                    .verify();
        }
    }

    // ==================== Diff Reporting Integration Tests ====================

    @Nested
    @DisplayName("Diff Reporting Integration")
    class DiffReportingIntegration {

        @Test
        @DisplayName("Generate diff report for single entity change")
        void diffReport_singleChange() {
            AssertableEntity before = new AssertableEntity(1L, "Before", 10,
                    Optional.of("Desc"), Optional.of(5));
            AssertableEntity after = new AssertableEntity(1L, "After", 10,
                    Optional.of("Desc"), Optional.of(5));

            var diff = AssertableEntityDiff.compare(before, after);

            assertTrue(diff.hasChanges());
            assertEquals(1, diff.changeCount());
            assertTrue(diff.isFieldChanged("name"));
            assertFalse(diff.isFieldChanged("count"));
        }

        @Test
        @DisplayName("Generate batch diff report for multiple entities")
        void diffReport_batch() {
            BatchDiffReport batch = new BatchDiffReport();

            // Add multiple entity diffs
            batch.add(AssertableEntityDiff.compare(
                    new AssertableEntity(1L, "A", 10, Optional.empty(), Optional.empty()),
                    new AssertableEntity(1L, "B", 10, Optional.empty(), Optional.empty())));
            batch.add(AssertableEntityDiff.compare(
                    new AssertableEntity(2L, "X", 20, Optional.of("D"), Optional.empty()),
                    new AssertableEntity(2L, "Y", 30, Optional.of("D"), Optional.empty())));
            batch.add(AssertableEntityDiff.compare(
                    new AssertableEntity(3L, "Same", 5, Optional.empty(), Optional.empty()),
                    new AssertableEntity(3L, "Same", 5, Optional.empty(), Optional.empty())));

            assertEquals(2, batch.changedEntityCount());
            assertEquals(3, batch.totalFieldChanges());  // name+name+count

            String report = batch.toCompactReport();
            assertTrue(report.contains("2 entities changed"));
        }

        @Test
        @DisplayName("Diff report generates assertion code")
        void diffReport_generatesAssertionCode() {
            var diff = AssertableEntityDiff.compare(
                    new AssertableEntity(1L, "Old", 10, Optional.empty(), Optional.empty()),
                    new AssertableEntity(1L, "New", 20, Optional.empty(), Optional.empty()));

            String code = diff.toAssertionCode();
            assertTrue(code.contains("assertEntityWithSnapshot"));
            assertTrue(code.contains("name"));
            assertTrue(code.contains("count"));
        }
    }

    // ==================== Error Reporting Integration Tests ====================

    @Nested
    @DisplayName("Error Reporting Integration")
    class ErrorReportingIntegration {

        @Test
        @DisplayName("Error report shows path to mismatched field")
        void errorReport_showsFieldPath() {
            try {
                takeSnapshot();
                TierOneType obj = new TierOneType("actual");
                assertObject(obj, value("property", "expected"));
                fail("Should have thrown");
            } catch (AssertionFailedError e) {
                String message = e.getMessage();
                assertTrue(message.contains("property"), "Message should contain field name");
                assertTrue(message.contains("expected") || message.contains("actual"),
                        "Message should contain values");
            }
        }

        @Test
        @DisplayName("Error report for nested field mismatch")
        void errorReport_nestedFieldMismatch() {
            try {
                takeSnapshot();
                EntityTierTwoType obj = new EntityTierTwoType();
                obj.setId(1);
                obj.setProperty("outer");
                EntityTierOneType inner = new EntityTierOneType("actual", 10);
                obj.setSubProperty(inner);
                customers.add(obj);
                orders.add(inner);

                assertObject(obj,
                        value("id", 1),
                        value("property", "outer"),
                        value("subProperty.id", 10),
                        value("subProperty.property", "expected"));  // mismatch here
                fail("Should have thrown");
            } catch (AssertionFailedError e) {
                String message = e.getMessage();
                assertTrue(message.contains("property"),
                        "Message should contain field path");
            }
        }
    }

    // ==================== Concurrent Modification Scenarios ====================

    @Nested
    @DisplayName("Real-World Service Simulation")
    class ServiceSimulation {

        @Test
        @DisplayName("Simulate order processing workflow")
        void simulateOrderProcessing() {
            // Initial setup: customer and existing order
            EntityTierOneType existingOrder = new EntityTierOneType("DRAFT", 1);
            orders.add(existingOrder);
            takeSnapshot();

            // Service: Submit order
            existingOrder.setProperty("SUBMITTED");

            // Service: Create shipment (new entity)
            EntityTierOneType shipment = new EntityTierOneType("PENDING_SHIPMENT", 100);
            orders.add(shipment);

            // Assertions
            assertEntityWithSnapshot(existingOrder, value("property", "SUBMITTED"));
            assertObject(shipment, value("id", 100), value("property", "PENDING_SHIPMENT"));
        }

        @Test
        @DisplayName("Simulate cascade delete")
        void simulateCascadeDelete() {
            // Setup: parent with child entities
            EntityTierOneType order1 = new EntityTierOneType("ORDER1", 1);
            EntityTierOneType order2 = new EntityTierOneType("ORDER2", 2);
            EntityTierTwoType customer = new EntityTierTwoType("Customer", 10, order1);
            orders.add(order1);
            orders.add(order2);
            customers.add(customer);
            takeSnapshot();

            // Cascade delete: remove customer and associated order
            customers.remove(customer);
            orders.remove(order1);

            // Assert both deletions
            assertEntityAsDeleted(customer);
            assertEntityAsDeleted(order1);
        }

        @Test
        @DisplayName("Simulate batch update")
        void simulateBatchUpdate() {
            // Setup: multiple entities
            EntityTierOneType order1 = new EntityTierOneType("PENDING", 1);
            EntityTierOneType order2 = new EntityTierOneType("PENDING", 2);
            EntityTierOneType order3 = new EntityTierOneType("PENDING", 3);
            orders.add(order1);
            orders.add(order2);
            orders.add(order3);
            takeSnapshot();

            // Batch update: mark all as processed
            order1.setProperty("PROCESSED");
            order2.setProperty("PROCESSED");
            order3.setProperty("PROCESSED");

            // Assert all updates
            assertEntityWithSnapshot(order1, value("property", "PROCESSED"));
            assertEntityWithSnapshot(order2, value("property", "PROCESSED"));
            assertEntityWithSnapshot(order3, value("property", "PROCESSED"));
        }
    }
}
