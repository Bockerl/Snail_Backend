@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

class CommandMemberServiceImplTests
//    :
//    BehaviorSpec({
//        // mock
//        val memberRepository = mockk<MemberRepository>()
//        val activityAreaRepository = mockk<ActivityAreaRepository>()
//        val commandFileService = mockk<CommandFileService>()
//        // test 구현체
//        val memberService =
//            CommandMemberServiceImpl(
//                memberRepository,
//                activityAreaRepository,
//                commandFileService,
//            )
//
//        // 마지막 접속 시간 업데이트 테스트
//        Given("사용자가 로그인해서") {
//            val email = TEST_EMAIL
//            val wrongEmail = "wrong_email"
//            val beforeTime = LocalDateTime.now().minusDays(1)
//            val member = createMember()
//            val memberSlot = slot<Member>()
//            every { memberRepository.findMemberByMemberEmail(email) }.returns(member)
//            every { memberRepository.save(capture(memberSlot)) }.returns(member)
//
//            When("마지막 접속 시간이 업데이트 될 때") {
//                memberService.putLastAccessTime(email)
//
//                Then("회원의 마지막 접속 시간이 업데이트되어야 한다.") {
//                    verify(exactly = 1) { memberRepository.findMemberByMemberEmail(email) }
//                    verify(exactly = 1) { memberRepository.save(any()) }
//
//                    val savedMember = memberSlot.captured
//                    savedMember.lastAccessTime shouldBe after(beforeTime)
//                }
//            }
//
//            When("존재하지 않은 이메일로 서비스가 호출되면") {
//                every { memberRepository.findMemberByMemberEmail(wrongEmail) }.returns(null)
//
//                Then("존재하지 않는 회원 예외를 반환한다.") {
//                    val exception =
//                        shouldThrow<CommonException> {
//                            memberService.putLastAccessTime(wrongEmail)
//                        }
//                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_MEMBER
//                }
//            }
//
//            When("마지막 접속 시간 저장 과정에서 오류가 발생하면") {
//                every { memberRepository.findMemberByMemberEmail(email) } returns (member)
//                every { memberRepository.save(any()) } throws DataIntegrityViolationException("Database Error")
//
//                Then("서버 오류 예외가 발생한다.") {
//                    val exception =
//                        shouldThrow<CommonException> {
//                            memberService.putLastAccessTime(email)
//                        }
//                    exception.errorCode shouldBe ErrorCode.INTERNAL_SERVER_ERROR
//                }
//            }
//        }
//    })