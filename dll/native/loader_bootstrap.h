#ifndef VAPE421_LOADER_BOOTSTRAP_H
#define VAPE421_LOADER_BOOTSTRAP_H

int vape_loader_bootstrap_initialize(void);
void vape_loader_report_progress(int step);
void vape_loader_report_completed(void);
void vape_loader_report_failure(const char *message);
void vape_loader_bootstrap_clear(void);

#endif
