package se.group.projektarbete.service;

import org.springframework.stereotype.Service;
import se.group.projektarbete.data.User;
import se.group.projektarbete.data.WorkItem;
import se.group.projektarbete.repository.UserRepository;
import se.group.projektarbete.repository.WorkItemRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public final class UserService {

    private UserRepository userRepository;
    private WorkItemRepository workItemRepository;
    private AtomicLong userNumbers;

    public UserService(UserRepository userRepository, WorkItemRepository workItemRepository) {
        this.userRepository = userRepository;
        this.workItemRepository = workItemRepository;
        userNumbers = new AtomicLong(this.userRepository.getHighestUserNumber().orElse(1000L));
    }

    public User createUser(User user) {
        validateUser(user);
        return userRepository.save(new User(
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                userNumbers.incrementAndGet()));
    }

    public Optional<User> getUserByUsernumber(Long userNumber) {
        return userRepository.findUserByuserNumber(userNumber);
    }

    public Boolean updateUser(Long userNumber, User user) {
        validateUser(user);
        if (userRepository.findUserByuserNumber(userNumber).isPresent()) {
            Optional<User> users = userRepository.findUserByuserNumber(userNumber);
            users.get().updateUser(user);
            userRepository.save(users.get());
            return true;
        }
        return false;
    }

    public Boolean inactivateUser(Long userNumber) {
        if (userRepository.findUserByuserNumber(userNumber).isPresent()) {
            Optional<User> users = userRepository.findUserByuserNumber(userNumber);
            users.get().setActive(false);
            userRepository.save(users.get());
            List<WorkItem> workItems = workItemRepository.findAllByUser(users.get());
            setWorkItemsToUnstarted(workItems, users.get());
            return true;
        }
        return false;
    }

    private void setWorkItemsToUnstarted(List<WorkItem> workItems, User user) {
        if (!workItems.isEmpty()) {
            user.setWorkItemsToUnstarted(workItems);
            saveWorkItems(workItems);
        }
    }

    private void saveWorkItems(List<WorkItem> workItems) {
        for (int i = 0; i < workItems.size(); i++) {
            workItemRepository.save(workItems.get(i));
        }
    }

    private void validateUser(User user) {
        if (user.getUserName().length() < 10) {
            throw new InvalidInputException("Username cannot be shorter than 10 characters");
        }
    }
}
