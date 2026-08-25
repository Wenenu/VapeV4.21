#include "loader_bootstrap.h"

int main(void) {
    if (!vape_loader_bootstrap_initialize()) {
        return 1;
    }
    vape_loader_report_progress(23);
    vape_loader_report_completed();
    vape_loader_report_failure("local test");
    vape_loader_bootstrap_clear();
    return 0;
}
