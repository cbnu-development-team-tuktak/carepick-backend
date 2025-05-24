package com.callrapport.dto

// 병원 관련 엔티티 import
import com.callrapport.model.hospital.Hospital // 병원 기본 정보 엔티티 
import com.callrapport.model.hospital.HospitalAdditionalInfo // 병원 추가 정보 엔티티
import com.callrapport.model.hospital.HospitalImage // 병원 이미지 엔티티
import com.callrapport.model.hospital.HospitalOperatingHours // 병원 진료 시간 엔티티
import com.callrapport.model.hospital.OperatingHours // 진료 시간 엔티티

// 공간 좌표 관련 import  
import org.locationtech.jts.geom.Point // 공간 좌표(Point) 타입

// 시간 관련 import
import java.time.format.DateTimeFormatter // 시간 포맷터

data class HospitalDetailsResponse(
    val id: String, // 병원 ID
    val name: String, // 병원명
    val phoneNumber: String?, // 병원 전화번호 (선택적 필드, NULL 허용)
    val homepage: String?, // 병원 홈페이지 URL (선택적 필드, NULL 허용)
    val address: String, // 병원 주소
    val operatingHours: List<OperatingHourResponse>?, // 병원 운영 시간 (선택적 필드, NULL 허용)
    val url: String, // 병원 정보 페이지 URL
    val specialties: List<String>?, // 병원의 진료과 목록 (선택적 필드, NULL 허용)
    val doctors: List<String>?, // 병원에 소속된 의사 목록 (선택적 필드, NULL 허용)
    val images: List<ImageResponse>?, // 병원과 연결된 이미지 정보 추가
    val additionalInfo: HospitalAdditionalInfo?, // 병원 추가 정보 (선택적 필드, NULL 허용)
    val location: LatLng?
) {
    companion object {
        // Hospital 엔티티를 HospitalDetailsResponse DTO로 변환
        fun from(hospital: Hospital): HospitalDetailsResponse {
            return HospitalDetailsResponse(
                id = hospital.id, // 병원 ID
                name = hospital.name, // 병원명
                phoneNumber = hospital.phoneNumber, // 병원 전화번호
                homepage = hospital.homepage, // 병원 홈페이지 URL
                address = hospital.address ?: "", // 병원 주소 (주소가 null인 경우 빈 문자열로 대체)
                operatingHours = hospital.operatingHours
                    .takeIf { it.isNotEmpty() }
                    ?.map { 
                        // 변경된 OperatingHourResponse에 맞춰 변환
                        OperatingHourResponse.from(it.operatingHours)
                    },
                url = hospital.url ?: "",
                // 병원에 연결된 진료과 목록이 존재할 경우
                specialties = hospital.specialties
                    .takeIf { it.isNotEmpty() } // 비어 있지 않은 경우에만 수행
                    ?.mapNotNull { it.specialty?.name }, // specialty가 null이 아니면 name 추출

                // 병원에 연결된 의사 목록이 존재할 경우
                doctors = hospital.doctors
                    .takeIf { it.isNotEmpty() } // 비어 있지 않은 경우에만 수행
                    ?.mapNotNull { it.doctor?.id }, // doctor가 null이 아니면 id 추출

                // 병원 이미지가 존재할 경우
                images = hospital.images
                    .takeIf { it.isNotEmpty() } // 비어 있지 않은 경우에만  수행
                    ?.map { ImageResponse.from(it) }, // ImageResponse로 변환

                additionalInfo = hospital.additionalInfo, // 병원 부가 정보
                
                location = hospital.location?.toLatLng()
            )
        }
    }
}

// 병원 이미지 정보를 담을 서브 DTO
data class ImageResponse(
    val id: Long, // 이미지 ID
    val url: String, // 이미지 URL
    val alt: String // 이미지 대체 텍스트
) {
    companion object {
        // HospitalImage 엔티티를 ImageResponse DTO로 변환
        fun from(hospitalImage: HospitalImage): ImageResponse {
            return ImageResponse(
                // 이미지 ID가 null인 경우 기본값으로 0L 설정
                id = hospitalImage.image.id ?: 0L, 
                
                // 실제 이미지가 위치한 외부 URL
                url = hospitalImage.image.url,

                // 해당 이미지에 대한 설명
                alt = hospitalImage.image.alt
            )
        }
    }
}

// 병원 좌표 정보를 담을 클래스
data class LatLng(
    val latitude: Double, // 위도
    val longitude: Double // 경도
)

// Point 객체를 위도·경도(LatLng) 형태로 변환
fun Point?.toLatLng(): LatLng? {
    return this?.let {
        // y = 위도, x = 경도
        LatLng(latitude = it.y, longitude = it.x)
    }
}

data class OperatingHourResponse(
    val day: String, // 요일 (예: "월")
    val startTime: String?, // 시작 시간 ("HH:mm" 형식)
    val endTime: String?, // 종료 시간 ("HH:mm" 형식)
) {
    companion object {
        fun from(operatingHours: OperatingHours): OperatingHourResponse {
            val formatter = DateTimeFormatter.ofPattern("HH:mm") // 시간 형식 포맷터 정의

            val formattedStart = operatingHours.startTime?.format(formatter) // 시작 시간을 문자열로 포맷
            val formattedEnd = operatingHours.endTime?.format(formatter) // 종료 시간을 문자열로 포맷

            return OperatingHourResponse(
                day = operatingHours.day, // 요일 설정 
                startTime = formattedStart, // 시작 시간
                endTime = formattedEnd, // 종료 시간
            )
        }
    }
}
