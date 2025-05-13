@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.member.command.application.dto.request.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class RegistrationServiceImplMockitoTests
//    : TestSupport() {
//    @Mock
//    private lateinit var tempMemberRepository: TempMemberRepository
//
//    @Mock
//    private lateinit var authService: AuthService
//
//    @Mock
//    private lateinit var memberRepository: MemberRepository
//
//    @Mock
//    private lateinit var activityAreaRepository: ActivityAreaRepository
//
//    @Mock
//    private lateinit var bcryptPasswordEncoder: BCryptPasswordEncoder
//
//    private lateinit var registrationService: RegistrationService
//
//    @BeforeEach
//    fun setUp() {
//        registrationService =
//            RegistrationServiceImpl(
//                authService,
//                tempMemberRepository,
//                memberRepository,
//                activityAreaRepository,
//                bcryptPasswordEncoder,
//            )
//    }
//
//    companion object {
//        private const val TEST_EMAIL = "test@test.com"
//        private const val TEST_NICKNAME = "testUser"
//        private const val TEST_PHONE = "01012345678"
//        private const val TEST_PASSWORD = "Password123!"
//        private const val TEST_REDIS_ID = "test-redis-id"
//        private const val VERIFICATION_CODE = "12345"
//        private val TEST_BIRTH = Timestamp(1708300800000)
//        private const val TEST_PRIMARY_AREA = "Emd-00000001"
//        private const val TEST_WORKPLACE_AREA = "Emd-00000002"
//    }
//
//    @Nested
//    @DisplayName("회원가입 시작 - 이메일")
//    inner class InitiateRegistration {
//        @Test
//        @DisplayName("회원가입 초기화 성공")
//        fun initiateRegistration_success() {
//            // given
//            val request =
//                EmailRequestDTO(
//                    memberEmail = TEST_EMAIL,
//                    memberNickName = TEST_NICKNAME,
//                    memberBirth = TEST_BIRTH,
//                )
//            whenever(tempMemberRepository.save(any())).thenReturn(TEST_REDIS_ID)
//            doNothing().`when`(authService).createEmailVerificationCode(TEST_EMAIL)
//
//            // when
//            val result = registrationService.initiateRegistration(request)
//
//            // then
//            assertEquals(TEST_REDIS_ID, result)
//            verify(tempMemberRepository).save(
//                argThat { tempMember ->
//                    tempMember.email == TEST_EMAIL &&
//                        tempMember.nickName == TEST_NICKNAME &&
//                        tempMember.birth == TEST_BIRTH &&
//                        tempMember.signUpStep == SignUpStep.INITIAL
//                },
//            )
//        }
//    }
//
//    @Nested
//    @DisplayName("이메일 인증 관련 테스트")
//    inner class EmailVerification {
//        @Test
//        @DisplayName("이메일 코드 재요청 - 성공")
//        fun emailVerification_refresh_success() {
//            // given
//            val tempMember =
//                TempMember.initiate(
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(tempMember)
//            doNothing().`when`(authService).createEmailVerificationCode(TEST_EMAIL)
//
//            // when & then
//            assertDoesNotThrow {
//                registrationService.createEmailRefreshCode(TEST_REDIS_ID)
//            }
//            verify(authService).createEmailVerificationCode(TEST_EMAIL)
//        }
//
//        @Test
//        @DisplayName("이메일 코드 재요청 실패 - 만료된 세션")
//        fun emailVerification_refresh_failure_expired() {
//            // given
//            val requestDTO =
//                PhoneRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    phoneNumber = TEST_PHONE,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(null)
//
//            // when & then
//            val exception =
//                assertThrows<CommonException> {
//                    registrationService.createPhoneRefreshCode(requestDTO)
//                }
//            assertEquals(exception.errorCode, ErrorCode.EXPIRED_SIGNUP_SESSION)
//        }
//
//        @Test
//        @DisplayName("이메일 코드 재요청 실패 - 잘못된 순서")
//        fun emailVerification_refresh_failure_unauthorized() {
//            // given
//            val tempMember =
//                TempMember.initiate(
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                )
//            val wrongTempMember =
//                tempMember.copy(
//                    signUpStep = SignUpStep.EMAIL_VERIFIED,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(wrongTempMember)
//
//            // when & then
//            val exception =
//                assertThrows<CommonException> {
//                    registrationService.createEmailRefreshCode(TEST_REDIS_ID)
//                }
//            assertEquals(exception.errorCode, ErrorCode.UNAUTHORIZED_ACCESS)
//        }
//
//        @Test
//        @DisplayName("이메일 인증 요청 - 성공")
//        fun emailVerification_success() {
//            // given
//            val request =
//                EmailVerifyRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    verificationCode = VERIFICATION_CODE,
//                )
//            val tempMember =
//                TempMember.initiate(
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(tempMember)
//            doNothing().`when`(authService).verifyCode(any(), any(), any())
//            doNothing().`when`(tempMemberRepository).update(any(), any())
//
//            // when
//            val result = registrationService.verifyEmailCode(request)
//
//            // then
//            // 반환값 검증
//            assertEquals(result, TEST_REDIS_ID)
//            // 인증 서비스 검증
//            verify(authService).verifyCode(
//                eq(TEST_EMAIL),
//                eq(VERIFICATION_CODE),
//                eq(VerificationType.EMAIL),
//            )
//            // redis 저장 검증
//            verify(tempMemberRepository).update(
//                eq(TEST_REDIS_ID),
//                argThat { updatedTempMember ->
//                    updatedTempMember.email == TEST_EMAIL &&
//                        updatedTempMember.nickName == TEST_NICKNAME &&
//                        updatedTempMember.birth == TEST_BIRTH &&
//                        updatedTempMember.signUpStep == SignUpStep.EMAIL_VERIFIED
//                },
//            )
//        }
//
//        @Test
//        @DisplayName("이메일 인증 요청 실패 - 세션 만료")
//        fun emailVerification_failure_expired() {
//            // given
//            val request =
//                EmailVerifyRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    verificationCode = VERIFICATION_CODE,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(null)
//
//            // when & then
//            val exception =
//                assertThrows<CommonException> {
//                    registrationService.verifyEmailCode(request)
//                }
//            assertEquals(exception.errorCode, ErrorCode.EXPIRED_SIGNUP_SESSION)
//        }
//
//        @Test
//        @DisplayName("이메일 인증 요청 실패 - 잘못된 순서")
//        fun emailVerification_failure_unauthorized() {
//            // given
//            val request =
//                EmailVerifyRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    verificationCode = VERIFICATION_CODE,
//                )
//            val tempMember =
//                TempMember(
//                    redisId = TEST_REDIS_ID,
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                    signUpStep = SignUpStep.EMAIL_VERIFIED,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(tempMember)
//
//            val exception =
//                assertThrows<CommonException> {
//                    registrationService.verifyEmailCode(request)
//                }
//            assertEquals(exception.errorCode, ErrorCode.UNAUTHORIZED_ACCESS)
//        }
//    }
//
//    @Nested
//    @DisplayName("휴대폰 인증 관련 테스트")
//    inner class PhoneVerification {
//        @Test
//        @DisplayName("휴대폰 인증 코드 요청 성공")
//        fun phoneVerificationCode_create_success() {
//            val request =
//                PhoneRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    phoneNumber = TEST_PHONE,
//                )
//            val tempMember =
//                TempMember(
//                    redisId = TEST_REDIS_ID,
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                    signUpStep = SignUpStep.EMAIL_VERIFIED,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(tempMember)
//            whenever(authService.createPhoneVerificationCode(TEST_PHONE)).thenReturn(VERIFICATION_CODE)
//            doNothing().`when`(tempMemberRepository).update(any(), any())
//
//            // when
//            val result = registrationService.createPhoneVerificationCode(request)
//
//            // then
//            assertEquals(result, VERIFICATION_CODE)
//            verify(authService).createPhoneVerificationCode(
//                eq(TEST_PHONE),
//            )
//            verify(tempMemberRepository).update(
//                eq(TEST_REDIS_ID),
//                argThat { updatedMember ->
//                    updatedMember.email == TEST_EMAIL &&
//                        updatedMember.nickName == TEST_NICKNAME &&
//                        updatedMember.birth == TEST_BIRTH &&
//                        updatedMember.phoneNumber == TEST_PHONE &&
//                        updatedMember.signUpStep == SignUpStep.EMAIL_VERIFIED
//                },
//            )
//        }
//
//        @Test
//        @DisplayName("핸드폰 인증 코드 재요청 - 성공")
//        fun phoneVerification_refresh_success() {
//            // given
//            val tempMember =
//                TempMember(
//                    redisId = TEST_REDIS_ID,
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                    phoneNumber = TEST_PHONE,
//                    signUpStep = SignUpStep.EMAIL_VERIFIED,
//                )
//            val requestDTO =
//                PhoneRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    phoneNumber = TEST_PHONE,
//                )
//
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(tempMember)
//            whenever(authService.createPhoneVerificationCode(TEST_PHONE)).thenReturn(VERIFICATION_CODE)
//
//            // when & then
//            assertDoesNotThrow {
//                registrationService.createPhoneRefreshCode(requestDTO)
//            }
//            verify(authService).createPhoneVerificationCode(TEST_PHONE)
//        }
//
//        @Test
//        @DisplayName("핸드폰 인증 - 성공")
//        fun phoneVerification_success() {
//            // given
//            val request =
//                PhoneVerifyRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    verificationCode = VERIFICATION_CODE,
//                )
//            val tempMember =
//                TempMember(
//                    redisId = TEST_REDIS_ID,
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                    phoneNumber = TEST_PHONE,
//                    signUpStep = SignUpStep.EMAIL_VERIFIED,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(tempMember)
//            doNothing().`when`(authService).verifyCode(any(), any(), any())
//            doNothing().`when`(tempMemberRepository).update(any(), any())
//
//            // when
//            val result = registrationService.verifyPhoneCode(request)
//
//            // then
//            assertEquals(result, TEST_REDIS_ID)
//            verify(authService).verifyCode(TEST_PHONE, VERIFICATION_CODE, VerificationType.PHONE)
//            verify(tempMemberRepository).update(
//                eq(TEST_REDIS_ID),
//                argThat { updatedMember ->
//                    updatedMember.email == TEST_EMAIL &&
//                        updatedMember.nickName == TEST_NICKNAME &&
//                        updatedMember.birth == TEST_BIRTH &&
//                        updatedMember.phoneNumber == TEST_PHONE &&
//                        updatedMember.signUpStep == SignUpStep.PHONE_VERIFIED
//                },
//            )
//        }
//    }
//
//    @Nested
//    @DisplayName("비밀번호 입력 관련 테스트")
//    inner class PasswordVerification {
//        @Test
//        @DisplayName("비밀번호 입력 - 성공")
//        fun passwordVerification_success() {
//            // given
//            val request =
//                PasswordRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    password = TEST_PASSWORD,
//                )
//            val tempMember =
//                TempMember(
//                    redisId = TEST_REDIS_ID,
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                    phoneNumber = TEST_PHONE,
//                    signUpStep = SignUpStep.PHONE_VERIFIED,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(tempMember)
//            doNothing().`when`(tempMemberRepository).update(any(), any())
//
//            // when
//            val result = registrationService.postPassword(request)
//
//            // then
//            assertEquals(result, TEST_REDIS_ID)
//            verify(tempMemberRepository).update(
//                eq(TEST_REDIS_ID),
//                argThat { updatedMember ->
//                    updatedMember.email == TEST_EMAIL &&
//                        updatedMember.nickName == TEST_NICKNAME &&
//                        updatedMember.birth == TEST_BIRTH &&
//                        updatedMember.phoneNumber == TEST_PHONE &&
//                        updatedMember.password == TEST_PASSWORD &&
//                        updatedMember.signUpStep == SignUpStep.PASSWORD_VERIFIED
//                },
//            )
//        }
//    }
//
//    @Nested
//    @DisplayName("활동지역 설정 관련 테스트")
//    inner class ActivityAreaVerification {
//        @Test
//        @DisplayName("활동지역 설정 성공 - 이메일 회원 가입 성공")
//        fun activityAreaVerification_success() {
//            // given
//            val request =
//                ActivityAreaRegisterRequestDTO(
//                    redisId = TEST_REDIS_ID,
//                    primaryFormattedId = TEST_PRIMARY_AREA,
//                    workplaceFormattedId = TEST_WORKPLACE_AREA,
//                )
//            val tempMember =
//                TempMember(
//                    redisId = TEST_REDIS_ID,
//                    email = TEST_EMAIL,
//                    nickName = TEST_NICKNAME,
//                    birth = TEST_BIRTH,
//                    phoneNumber = TEST_PHONE,
//                    password = TEST_PASSWORD,
//                    signUpStep = SignUpStep.PASSWORD_VERIFIED,
//                )
//            whenever(tempMemberRepository.find(TEST_REDIS_ID)).thenReturn(tempMember)
//            whenever(bcryptPasswordEncoder.encode(any())).thenAnswer { TEST_PASSWORD }
//            whenever(memberRepository.save(any<Member>())).thenAnswer { invocation ->
//                val newMember = invocation.getArgument<Member>(0)
//                newMember.apply { memberId = 1L }
//            }
//            whenever(activityAreaRepository.save(any<ActivityArea>())).thenAnswer { invocation ->
//                val newActivityArea = invocation.getArgument<ActivityArea>(0)
//                newActivityArea
//            }
//
//            // when
//            registrationService.postActivityArea(request)
//
//            // then
//            verify(memberRepository).save(
//                argThat { newMember ->
//                    newMember.memberId == 1L &&
//                        newMember.memberEmail == TEST_EMAIL &&
//                        newMember.memberBirth == TEST_BIRTH.toLocalDateTime().toLocalDate() &&
//                        newMember.memberNickname == TEST_NICKNAME &&
//                        newMember.memberPassword == TEST_PASSWORD &&
//                        newMember.memberPhoneNumber == TEST_PHONE
//                },
//            )
//            verify(activityAreaRepository).save(
//                argThat { primaryArea ->
//                    primaryArea.areaType == AreaType.PRIMARY &&
//                        primaryArea.id?.memberId == 1L &&
//                        primaryArea.id?.emdAreasId == 1L
//                },
//            )
//            verify(activityAreaRepository).save(
//                argThat { workplaceArea ->
//                    workplaceArea.areaType == AreaType.WORKPLACE &&
//                        workplaceArea.id?.memberId == 1L &&
//                        workplaceArea.id?.emdAreasId == 2L
//                },
//            )
//            verify(tempMemberRepository).delete(TEST_REDIS_ID)
//        }
//    }
// }