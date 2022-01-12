CREATE TABLE IF NOT EXISTS public.counter_status(
    id bigserial NOT NULL,
    status varchar NULL,
    CONSTRAINT counter_status_pkey PRIMARY KEY (id)
);
