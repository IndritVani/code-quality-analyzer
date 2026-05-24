package com.analyzer.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitRepositoryServiceTest {

    private final GitRepositoryService service = new GitRepositoryService();

    @Test
    void normalizesAcceptedForms() {
        String expected = "https://github.com/spring-projects/spring-petclinic.git";

        assertThat(service.normalizeGithubUrl("https://github.com/spring-projects/spring-petclinic"))
                .isEqualTo(expected);
        assertThat(service.normalizeGithubUrl("https://github.com/spring-projects/spring-petclinic.git"))
                .isEqualTo(expected);
        assertThat(service.normalizeGithubUrl("https://github.com/spring-projects/spring-petclinic/"))
                .isEqualTo(expected);
        assertThat(service.normalizeGithubUrl("  github.com/spring-projects/spring-petclinic  "))
                .isEqualTo(expected);
        assertThat(service.normalizeGithubUrl("spring-projects/spring-petclinic"))
                .isEqualTo(expected);
        assertThat(service.normalizeGithubUrl("https://WWW.GitHub.com/spring-projects/spring-petclinic"))
                .isEqualTo(expected);
    }

    @Test
    void rejectsBlankInput() {
        assertThatThrownBy(() -> service.normalizeGithubUrl(null))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.normalizeGithubUrl("   "))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsSshUrls() {
        assertThatThrownBy(() -> service.normalizeGithubUrl("git@github.com:owner/repo.git"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.normalizeGithubUrl("ssh://git@github.com/owner/repo"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsNonGithubAndMalformed() {
        assertThatThrownBy(() -> service.normalizeGithubUrl("https://gitlab.com/owner/repo"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.normalizeGithubUrl("https://github.com/owner"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.normalizeGithubUrl("not a url"))
                .isInstanceOf(BadRequestException.class);
    }
}
