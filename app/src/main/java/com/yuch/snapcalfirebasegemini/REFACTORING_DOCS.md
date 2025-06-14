## ViewModel Refactoring and Reorganization Documentation

### Overview
This documentation outlines the refactoring and reorganization of the SnapCal app's ViewModel and Repository layers to improve code quality, maintainability, and consistency.

### Key Changes

#### 1. ViewModels and Repositories Structure
- **FoodViewModel**: Consolidated functionality from `FoodViewModel` and `GetFoodViewModel` into a single, unified `FoodViewModel`
- **OnboardingViewModel**: Enhanced with calculation logic and better integration with `ProfileViewModel`
- **Repository Pattern**: Ensured all repositories follow consistent singleton pattern with `getInstance()`

#### 2. Factory Classes
- Organized ViewModelFactory classes into the `data/di` package
- Updated factories with consistent patterns:
  - `FoodViewModelFactory`: For creating the combined FoodViewModel
  - `ProfileViewModelFactory`: For ProfileViewModel 
  - `AuthViewModelFactory`: For AuthViewModel
  - `AiChatViewModelFactory`: For AiChatViewModel
  - `OnboardingViewModelFactory`: New factory for OnboardingViewModel
- Each factory now follows singleton pattern with `getInstance()` methods

#### 3. Connection Between ViewModels
- Created `ProfileOnboardingConnector` utility to facilitate data transfer between OnboardingViewModel and ProfileViewModel
- Made OnboardingViewModel optionally accept a ProfileRepository to enable direct saving

#### 4. Improved Error Handling and Documentation
- Added comprehensive documentation to all ViewModels
- Standardized error handling patterns
- Improved state management and flow organization

#### 5. FoodViewModel Consolidation
- Combined the best features from both FoodViewModel and GetFoodViewModel
- All food-related operations now go through a single, well-organized ViewModel
- Pagination, CRUD operations, and analytics all handled cohesively

### Design Decisions

1. **Repository/ViewModel Relationship**:
   - Each ViewModel should have a corresponding repository
   - Repositories handle data operations and are injected into ViewModels
   - ViewModels manage UI state and orchestrate repository calls

2. **Factory Pattern**:
   - All ViewModels are created through factories
   - Factories handle dependency injection and object creation
   - Factories follow singleton pattern for consistency

3. **State Management**:
   - Each ViewModel explicitly defines state values using MutableStateFlow/StateFlow
   - Clear separation between mutable internals (_stateX) and immutable public interfaces (stateX)
   - Status handling through sealed classes (e.g., ApiStatus)

### Benefits of the Refactoring

1. **Reduced Code Duplication**: Eliminated duplicate code across ViewModels
2. **Improved Testability**: Better separation of concerns makes unit testing easier
3. **Enhanced Maintainability**: Consistent patterns make the code easier to understand and modify
4. **Better Error Handling**: Standardized approach to handling and displaying errors
5. **Clearer API**: More intuitive API design for consuming components

### Next Steps
1. Update UI components to use the consolidated FoodViewModel
2. Create unit tests for the refactored ViewModels
3. Consider further integration of OnboardingViewModel and ProfileViewModel if business logic allows
4. Review and optimize data flow between repositories and UI
