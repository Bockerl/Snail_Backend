package com.bockerl.snailmember.file.command.domain.aggregate.entity

import jakarta.persistence.*

@Entity
@Table(name = "GatheringFile")
class GatheringFile(
    @Id
    @Column(name = "file_id")
    var fileId: Long,

    @Column(name = "gathering_id", nullable = false)
    var gatheringId: Long,

    @OneToOne(cascade = [(CascadeType.ALL)])
    @MapsId // fileId를 GatheringFile의 PK로 사용
    @JoinColumn(name = "file_id")
    var file: File,
)
