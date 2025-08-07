    package com.callrapport.repository.user

    import com.callrapport.model.user.Address
    import org.springframework.data.jpa.repository.JpaRepository
    import org.springframework.stereotype.Repository

    @Repository
    interface AddressRepository : JpaRepository<Address, Long>