#include "loader_bootstrap.h"

#include <windows.h>

enum {
    VAPE421_BOOTSTRAP_UNINITIALIZED = 0,
    VAPE421_BOOTSTRAP_READY = 1
};

static INIT_ONCE g_bootstrap_once = INIT_ONCE_STATIC_INIT;
static volatile LONG g_bootstrap_state = VAPE421_BOOTSTRAP_UNINITIALIZED;

static BOOL CALLBACK initialize_bootstrap(
        PINIT_ONCE once, PVOID parameter, PVOID *context) {
    (void)once;
    (void)parameter;
    (void)context;
    InterlockedExchange(&g_bootstrap_state, VAPE421_BOOTSTRAP_READY);
    return TRUE;
}

int vape_loader_bootstrap_initialize(void) {
    if (!InitOnceExecuteOnce(&g_bootstrap_once, initialize_bootstrap, NULL, NULL)) {
        return 0;
    }
    return InterlockedCompareExchange(&g_bootstrap_state, 0, 0)
            == VAPE421_BOOTSTRAP_READY;
}

void vape_loader_report_progress(int step) {
    (void)step;
}

void vape_loader_report_completed(void) {
}

void vape_loader_report_failure(const char *message) {
    (void)message;
}

void vape_loader_bootstrap_clear(void) {
    InterlockedExchange(&g_bootstrap_state, VAPE421_BOOTSTRAP_UNINITIALIZED);
}
