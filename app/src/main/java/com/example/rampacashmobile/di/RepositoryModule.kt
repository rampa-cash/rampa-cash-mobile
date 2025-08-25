package com.example.rampacashmobile.di

// File: app/src/main/java/com/example/rampacashmobile/di/RepositoryModule.kt

import com.example.rampacashmobile.data.repository.FakeOffRampRepository
import com.example.rampacashmobile.data.repository.OffRampRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // This component lives as long as the application
abstract class `RepositoryModule` {

    /**
     * This @Binds method tells Hilt that whenever an OffRampRepository is requested,
     * it should provide an instance of FakeOffRampRepository.
     *
     * Hilt knows how to create FakeOffRampRepository because FakeOffRampRepository
     * has an @Inject constructor.
     */
    @Binds
    @Singleton // Ensures that only one instance of FakeOffRampRepository is created and reused
    abstract fun bindOffRampRepository(
        // The parameter is the concrete implementation Hilt should provide.
        // The name of the parameter (e.g., fakeOffRampRepositoryImpl) doesn't strictly matter
        // for Hilt's functionality but should be descriptive.
        fakeOffRampRepositoryImpl: FakeOffRampRepository
    ): OffRampRepository // The return type is the interface or abstract class being bound.

    // --- Placeholder for your Real Repository Implementation ---
    // When you create your actual OffRampRepositoryImpl, you'll comment out or
    // remove the binding above and uncomment/add a binding like this:
    /*
    @Binds
    @Singleton
    abstract fun bindOffRampRepository(
        realOffRampRepositoryImpl: RealOffRampRepositoryImpl // Assuming you create this class
    ): OffRampRepository
    */
    //
    // And RealOffRampRepositoryImpl would look something like:
    // class RealOffRampRepositoryImpl @Inject constructor(
    //     private val transakApiService: TransakApiService, // Example external dependency
    //     private val bitsoApiService: BitsoApiService      // Example external dependency
    // ) : OffRampRepository {
    //     // ... Actual implementation using the API services ...
    // }
    // --- ---
}