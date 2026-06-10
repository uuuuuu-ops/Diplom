// config/DataInitializer.java
package com.diploma.Diplom.config;

import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.TeacherQuizQuestion;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.TeacherQuizQuestionRepository;
import com.diploma.Diplom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeacherQuizQuestionRepository teacherQuizQuestionRepository;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.email:admin@lms.com}")
    private String adminEmail;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                "APP_ADMIN_PASSWORD environment variable is not set. " +
                "Set it to a strong password before starting the application."
            );
        }

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setName("Admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setTeacherApproved(false);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            log.info("Default admin account created: {}", adminEmail);
        }

        seedTeacherQuizQuestions();
    }

    private void seedTeacherQuizQuestions() {
        if (teacherQuizQuestionRepository.count() > 0) {
            return;
        }

        List<TeacherQuizQuestion> questions = Arrays.asList(
                question("General", "What is the primary goal of formative assessment?",
                        List.of("To assign final grades", "To monitor student learning and provide ongoing feedback",
                                "To rank students against each other", "To replace final exams"),
                        1, "Formative assessment is used during instruction to monitor progress and guide teaching."),
                question("General", "Which approach best supports differentiated instruction?",
                        List.of("Teaching every student the same way", "Adjusting content, process, and product based on student needs",
                                "Only grouping students by age", "Avoiding group work entirely"),
                        1, "Differentiated instruction tailors content, process, and product to individual student needs."),
                question("General", "What is a key benefit of providing clear learning objectives at the start of a lesson?",
                        List.of("It guarantees higher test scores", "It helps students understand what they are expected to learn",
                                "It removes the need for assessment", "It shortens the lesson"),
                        1, "Clear objectives help students focus on the intended outcomes of the lesson."),
                question("General", "Which strategy is most effective for managing a diverse online classroom?",
                        List.of("Ignoring different learning paces", "Using varied content formats and flexible deadlines",
                                "Only using text-based materials", "Disabling student communication"),
                        1, "Varied formats and flexible deadlines accommodate different learning paces and styles."),
                question("General", "Why is constructive feedback important for student growth?",
                        List.of("It focuses only on mistakes", "It helps students understand strengths and areas to improve",
                                "It is only useful for advanced students", "It should be given once a year"),
                        1, "Constructive feedback highlights strengths and actionable areas for improvement."),
                question("General", "What does academic integrity primarily refer to?",
                        List.of("Following honest and ethical practices in learning and assessment", "Completing courses quickly",
                                "Memorizing course materials", "Avoiding group projects"),
                        0, "Academic integrity means upholding honesty and ethics in all academic work."),

                question("Programming", "What is the time complexity of binary search on a sorted array?",
                        List.of("O(n)", "O(log n)", "O(n^2)", "O(1)"),
                        1, "Binary search halves the search space each step, giving O(log n) complexity."),
                question("Programming", "Which data structure uses LIFO (Last In, First Out) ordering?",
                        List.of("Queue", "Stack", "Linked List", "Hash Map"),
                        1, "A stack follows Last In, First Out ordering."),
                question("Programming", "What is the main purpose of version control systems like Git?",
                        List.of("To compile code faster", "To track changes and collaborate on code over time",
                                "To run automated tests only", "To deploy applications to production"),
                        1, "Version control tracks code history and enables collaboration."),
                question("Programming", "In object-oriented programming, what does encapsulation refer to?",
                        List.of("Combining multiple classes into one file", "Bundling data and methods that operate on that data within a single unit",
                                "Running code in parallel", "Inheriting behavior from a parent class"),
                        1, "Encapsulation bundles data and the methods that operate on it within a class."),
                question("Programming", "Which of the following best describes a REST API?",
                        List.of("A database query language", "An architectural style for designing networked applications using HTTP",
                                "A programming language", "A type of compiler"),
                        1, "REST is an architectural style for networked applications, typically over HTTP."),

                question("Mathematics", "What is the derivative of x^2 with respect to x?",
                        List.of("x", "2x", "x^2", "2"),
                        1, "The derivative of x^2 is 2x by the power rule."),
                question("Mathematics", "What is the value of pi rounded to two decimal places?",
                        List.of("3.41", "3.14", "3.12", "3.16"),
                        1, "Pi is approximately equal to 3.14."),
                question("Mathematics", "Which of these is a prime number?",
                        List.of("21", "27", "29", "33"),
                        2, "29 is only divisible by 1 and itself, making it prime."),
                question("Mathematics", "What is the sum of the interior angles of a triangle?",
                        List.of("90 degrees", "180 degrees", "270 degrees", "360 degrees"),
                        1, "The interior angles of a triangle always sum to 180 degrees."),
                question("Mathematics", "Solve for x: 2x + 6 = 14",
                        List.of("2", "4", "6", "8"),
                        1, "2x + 6 = 14 -> 2x = 8 -> x = 4."),

                question("Physics", "What is the SI unit of force?",
                        List.of("Joule", "Newton", "Watt", "Pascal"),
                        1, "The SI unit of force is the Newton."),
                question("Physics", "Which law states that for every action there is an equal and opposite reaction?",
                        List.of("Newton's First Law", "Newton's Second Law", "Newton's Third Law", "Law of Conservation of Energy"),
                        2, "Newton's Third Law describes equal and opposite reactions to forces."),
                question("Physics", "What type of energy is stored in a stretched spring?",
                        List.of("Kinetic energy", "Thermal energy", "Potential energy", "Chemical energy"),
                        2, "A stretched spring stores potential (elastic) energy."),
                question("Physics", "What happens to the resistance of a conductor as its temperature increases (for most metals)?",
                        List.of("It decreases", "It increases", "It stays the same", "It becomes zero"),
                        1, "For most metals, resistance increases as temperature increases."),
                question("Physics", "What is the speed of light in a vacuum approximately?",
                        List.of("3 x 10^5 m/s", "3 x 10^6 m/s", "3 x 10^8 m/s", "3 x 10^10 m/s"),
                        2, "The speed of light in a vacuum is approximately 3 x 10^8 m/s."),

                question("Sciences", "What is the basic unit of life?",
                        List.of("Atom", "Cell", "Tissue", "Organ"),
                        1, "The cell is the basic structural and functional unit of life."),
                question("Sciences", "Which gas do plants primarily absorb for photosynthesis?",
                        List.of("Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen"),
                        2, "Plants absorb carbon dioxide for photosynthesis."),
                question("Sciences", "What is the chemical symbol for water?",
                        List.of("H2O", "O2", "CO2", "NaCl"),
                        0, "Water's chemical formula is H2O."),
                question("Sciences", "Which organ is primarily responsible for pumping blood through the body?",
                        List.of("Lungs", "Liver", "Heart", "Kidneys"),
                        2, "The heart pumps blood throughout the circulatory system."),
                question("Sciences", "What process do organisms use to break down nutrients and release energy?",
                        List.of("Photosynthesis", "Respiration", "Evaporation", "Fermentation only"),
                        1, "Cellular respiration breaks down nutrients to release energy.")
        );

        teacherQuizQuestionRepository.saveAll(questions);
        log.info("Seeded {} teacher quiz questions", questions.size());
    }

    private TeacherQuizQuestion question(String topic, String text, List<String> options, int correctIndex, String explanation) {
        TeacherQuizQuestion q = new TeacherQuizQuestion();
        q.setTopic(topic);
        q.setQuestion(text);
        q.setOptions(options);
        q.setCorrectIndex(correctIndex);
        q.setExplanation(explanation);
        return q;
    }
}