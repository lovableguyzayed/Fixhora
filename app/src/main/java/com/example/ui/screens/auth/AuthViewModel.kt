package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthResult
import com.example.data.repository.UserRepository
import com.example.data.session.SessionManager
import com.example.util.FieldError
import com.example.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Why a whole form failed, as opposed to a single field. */
enum class FormError {
  INVALID_CREDENTIALS,
  MOBILE_ALREADY_REGISTERED,
  NO_ACCOUNT_FOR_MOBILE,
  OTP_MISMATCH,
  UNEXPECTED,
}

data class SignInState(
  val mobile: String = "",
  val password: String = "",
  val mobileError: FieldError? = null,
  val passwordError: FieldError? = null,
  val formError: FormError? = null,
  val isSubmitting: Boolean = false,
  val signedIn: Boolean = false,
)

data class SignUpState(
  val fullName: String = "",
  val mobile: String = "",
  val email: String = "",
  val password: String = "",
  val confirmPassword: String = "",
  val agreedToTerms: Boolean = false,
  val fullNameError: FieldError? = null,
  val mobileError: FieldError? = null,
  val emailError: FieldError? = null,
  val passwordError: FieldError? = null,
  val confirmPasswordError: FieldError? = null,
  val termsNotAccepted: Boolean = false,
  val formError: FormError? = null,
  val isSubmitting: Boolean = false,
  val registered: Boolean = false,
)

data class ProfileState(
  val fullName: String = "",
  val gender: String = "",
  val dateOfBirth: String = "",
  val city: String = "",
  val state: String = "",
  val pinCode: String = "",
  val preferredLanguage: String = "",
  val fullNameError: FieldError? = null,
  val pinCodeError: FieldError? = null,
  val isSubmitting: Boolean = false,
  val saved: Boolean = false,
)

/**
 * Backs every screen in the auth graph.
 *
 * Each destination gets its own instance (it is created per `NavBackStackEntry`), so the three
 * state objects above never interfere with one another; the only value that crosses screens is the
 * mobile number, which travels as a navigation argument.
 */
class AuthViewModel(
  private val userRepository: UserRepository,
  private val sessionManager: SessionManager,
) : ViewModel() {

  private val _signIn = MutableStateFlow(SignInState())
  val signIn: StateFlow<SignInState> = _signIn.asStateFlow()

  private val _signUp = MutableStateFlow(SignUpState())
  val signUp: StateFlow<SignUpState> = _signUp.asStateFlow()

  private val _profile = MutableStateFlow(ProfileState())
  val profile: StateFlow<ProfileState> = _profile.asStateFlow()

  // ---------------------------------------------------------------- sign in

  fun onSignInMobileChange(value: String) {
    _signIn.update { it.copy(mobile = value, mobileError = null, formError = null) }
  }

  fun onSignInPasswordChange(value: String) {
    _signIn.update { it.copy(password = value, passwordError = null, formError = null) }
  }

  fun submitSignIn() {
    val state = _signIn.value
    if (state.isSubmitting) return

    val mobileError = Validators.validateMobile(state.mobile)
    // The password is only checked for presence here: an existing account may predate the current
    // strength rules, and refusing to even attempt the sign-in would lock its owner out.
    val passwordError = if (state.password.isEmpty()) FieldError.REQUIRED else null
    if (mobileError != null || passwordError != null) {
      _signIn.update { it.copy(mobileError = mobileError, passwordError = passwordError) }
      return
    }

    _signIn.update { it.copy(isSubmitting = true, formError = null) }
    viewModelScope.launch {
      when (val result = userRepository.login(state.mobile, state.password)) {
        is AuthResult.Success -> {
          sessionManager.signIn(result.user.id)
          _signIn.update { it.copy(isSubmitting = false, signedIn = true) }
        }
        AuthResult.InvalidCredentials ->
          _signIn.update {
            it.copy(isSubmitting = false, formError = FormError.INVALID_CREDENTIALS)
          }
        else -> _signIn.update { it.copy(isSubmitting = false, formError = FormError.UNEXPECTED) }
      }
    }
  }

  // ---------------------------------------------------------------- sign up

  fun onSignUpFullNameChange(value: String) {
    _signUp.update { it.copy(fullName = value, fullNameError = null, formError = null) }
  }

  fun onSignUpMobileChange(value: String) {
    _signUp.update { it.copy(mobile = value, mobileError = null, formError = null) }
  }

  fun onSignUpEmailChange(value: String) {
    _signUp.update { it.copy(email = value, emailError = null, formError = null) }
  }

  fun onSignUpPasswordChange(value: String) {
    _signUp.update { it.copy(password = value, passwordError = null, confirmPasswordError = null) }
  }

  fun onSignUpConfirmPasswordChange(value: String) {
    _signUp.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
  }

  fun onSignUpTermsChange(accepted: Boolean) {
    _signUp.update { it.copy(agreedToTerms = accepted, termsNotAccepted = false) }
  }

  fun prefillSignUpMobile(mobile: String) {
    if (mobile.isNotEmpty() && _signUp.value.mobile.isEmpty()) {
      _signUp.update { it.copy(mobile = mobile) }
    }
  }

  fun submitSignUp() {
    val state = _signUp.value
    if (state.isSubmitting) return

    val fullNameError = Validators.validateFullName(state.fullName)
    val mobileError = Validators.validateMobile(state.mobile)
    val emailError = Validators.validateEmailOptional(state.email)
    val passwordError = Validators.validatePassword(state.password)
    val confirmError =
      Validators.validatePasswordConfirmation(state.password, state.confirmPassword)
    val termsNotAccepted = !state.agreedToTerms

    if (
      fullNameError != null ||
        mobileError != null ||
        emailError != null ||
        passwordError != null ||
        confirmError != null ||
        termsNotAccepted
    ) {
      _signUp.update {
        it.copy(
          fullNameError = fullNameError,
          mobileError = mobileError,
          emailError = emailError,
          passwordError = passwordError,
          confirmPasswordError = confirmError,
          termsNotAccepted = termsNotAccepted,
        )
      }
      return
    }

    _signUp.update { it.copy(isSubmitting = true, formError = null) }
    viewModelScope.launch {
      when (
        val result =
          userRepository.register(
            fullName = state.fullName,
            mobile = state.mobile,
            email = state.email,
            password = state.password,
          )
      ) {
        is AuthResult.Success -> {
          sessionManager.signIn(result.user.id)
          _signUp.update { it.copy(isSubmitting = false, registered = true) }
        }
        AuthResult.MobileAlreadyRegistered ->
          _signUp.update {
            it.copy(isSubmitting = false, formError = FormError.MOBILE_ALREADY_REGISTERED)
          }
        else -> _signUp.update { it.copy(isSubmitting = false, formError = FormError.UNEXPECTED) }
      }
    }
  }

  // ---------------------------------------------------------------- otp

  /** True when [mobile] already has an account and can therefore be signed in by OTP alone. */
  suspend fun accountExists(mobile: String): Boolean = userRepository.accountExists(mobile)

  /** Signs in a number whose OTP has already been verified. Returns false if there is no account. */
  suspend fun signInWithVerifiedMobile(mobile: String): Boolean {
    val user = userRepository.findByVerifiedMobile(mobile) ?: return false
    sessionManager.signIn(user.id)
    return true
  }

  suspend fun resetPassword(mobile: String, newPassword: String): Boolean =
    userRepository.resetPassword(mobile, newPassword) is AuthResult.Success

  // ---------------------------------------------------------------- profile

  fun onProfileFieldChange(
    fullName: String = _profile.value.fullName,
    gender: String = _profile.value.gender,
    dateOfBirth: String = _profile.value.dateOfBirth,
    city: String = _profile.value.city,
    state: String = _profile.value.state,
    pinCode: String = _profile.value.pinCode,
    preferredLanguage: String = _profile.value.preferredLanguage,
  ) {
    _profile.update {
      it.copy(
        fullName = fullName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        city = city,
        state = state,
        pinCode = pinCode,
        preferredLanguage = preferredLanguage,
        fullNameError = null,
        pinCodeError = null,
      )
    }
  }

  /** Pre-fills the name captured at sign-up so the user does not type it twice. */
  fun loadProfileFromSession() {
    viewModelScope.launch {
      val userId = sessionManager.current().userId ?: return@launch
      val user = userRepository.findById(userId) ?: return@launch
      _profile.update {
        it.copy(
          fullName = it.fullName.ifEmpty { user.fullName },
          gender = it.gender.ifEmpty { user.gender.orEmpty() },
          dateOfBirth = it.dateOfBirth.ifEmpty { user.dateOfBirth.orEmpty() },
          city = it.city.ifEmpty { user.city },
          state = it.state.ifEmpty { user.state },
          pinCode = it.pinCode.ifEmpty { user.pinCode },
        )
      }
    }
  }

  fun submitProfile() {
    val state = _profile.value
    if (state.isSubmitting) return

    val fullNameError = Validators.validateFullName(state.fullName)
    val pinCodeError = Validators.validatePinCodeOptional(state.pinCode)
    if (fullNameError != null || pinCodeError != null) {
      _profile.update { it.copy(fullNameError = fullNameError, pinCodeError = pinCodeError) }
      return
    }

    _profile.update { it.copy(isSubmitting = true) }
    viewModelScope.launch {
      val userId = sessionManager.current().userId
      if (userId != null) {
        userRepository.updateProfile(
          userId = userId,
          fullName = state.fullName,
          gender = state.gender,
          dateOfBirth = state.dateOfBirth,
          city = state.city,
          state = state.state,
          pinCode = state.pinCode,
          preferredLanguage = state.preferredLanguage.ifEmpty { "en" },
        )
      }
      // With no session there is nothing to attach the profile to; the user still moves on rather
      // than being trapped on this screen.
      _profile.update { it.copy(isSubmitting = false, saved = true) }
    }
  }

  class Factory(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
        return AuthViewModel(userRepository, sessionManager) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
