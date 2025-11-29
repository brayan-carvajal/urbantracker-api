package com.sena.urbantracker.config.init;


import com.sena.urbantracker.security.domain.entity.RoleDomain;
import com.sena.urbantracker.security.domain.entity.UserDomain;
import com.sena.urbantracker.security.domain.repository.RoleRepository;
import com.sena.urbantracker.security.domain.repository.UserRepository;
import com.sena.urbantracker.users.domain.entity.CompanyDomain;
import com.sena.urbantracker.users.domain.entity.UserProfileDomain;
import com.sena.urbantracker.users.domain.repository.CompanyRepository;
import com.sena.urbantracker.users.domain.repository.UserProfileRepository;
import com.sena.urbantracker.vehicles.domain.entity.VehicleTypeDomain;
import com.sena.urbantracker.vehicles.domain.repository.VehicleTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;
    private final CompanyRepository companyRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        // Roles
        RoleDomain adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(
                        RoleDomain.builder()
                                .name("ADMIN")
                                .description("Tiene acceso completo al sistema")
                                .build()
                ));

        roleRepository.findByName("DRIVER")
                .orElseGet(() -> roleRepository.save(
                        RoleDomain.builder()
                                .name("DRIVER")
                                .description("Usuario con permisos limitados a las funcionalidades de conductor")
                                .build()
                ));

        // Usuario ADMIN
        String adminUsername = "admin";
        if (userRepository.findByUserName(adminUsername).isEmpty()) {
            UserDomain adminUser = new UserDomain();
            adminUser.setUserName(adminUsername);
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole(adminRole);
            adminUser = userRepository.save(adminUser);

            UserProfileDomain adminProfile = UserProfileDomain.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("urbantracker751@gmail.com")
                    .user(adminUser)
                    .build();
            userProfileRepository.save(adminProfile);

        }

        // Compañías de ejemplo
        if (companyRepository.findAll().isEmpty()) {
            companyRepository.save(CompanyDomain.builder()
                    .name("Transportes Urbanos S.A.")
                    .nit("901234567-1")
                    .country("Colombia")
                    .build());

            companyRepository.save(CompanyDomain.builder()
                    .name("Bus Rapid Transit Ltda.")
                    .nit("901234567-2")
                    .country("Colombia")
                    .build());

            companyRepository.save(CompanyDomain.builder()
                    .name("Metro Transporte")
                    .nit("901234567-3")
                    .country("Colombia")
                    .build());
        }

        // Tipos de vehículos de ejemplo
        if (vehicleTypeRepository.findAll().isEmpty()) {
            vehicleTypeRepository.save(VehicleTypeDomain.builder()
                    .name("Autobús Articulado")
                    .description("Vehículo de transporte público articulado")
                    .build());

            vehicleTypeRepository.save(VehicleTypeDomain.builder()
                    .name("Buseta")
                    .description("Vehículo mediano para rutas locales")
                    .build());

            vehicleTypeRepository.save(VehicleTypeDomain.builder()
                    .name("Microbús")
                    .description("Vehículo pequeño para rutas cortas")
                    .build());
        }

        System.out.println("✅ Usuario ADMIN creado (user: " + adminUsername + " | pass: admin123)");
        System.out.println("✅ Datos iniciales creados (compañías y tipos de vehículos)");
    }
}