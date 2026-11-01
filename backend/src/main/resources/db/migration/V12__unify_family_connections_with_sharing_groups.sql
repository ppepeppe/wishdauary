-- Reserve a separate group for previous family connections without changing the
-- audience of any existing GROUP diary or consuming one of the five custom slots.
alter table diary_sharing_groups drop constraint ck_diary_sharing_group_slot;
alter table diary_sharing_groups add constraint ck_diary_sharing_group_slot check (slot between 0 and 5);

insert into diary_sharing_groups (owner_id, name, slot)
select distinct c.user_id, '우리 가족', 0
from group_members c
join users child on child.id = c.user_id and child.app_role = 'CHILD'
where c.member_role = 'CHILD' and exists (
    select 1 from group_members f join users family on family.id = f.user_id
    where f.group_id = c.group_id and f.member_role = 'FAMILY' and family.app_role = 'FAMILY'
);

insert into diary_sharing_group_members (sharing_group_id, user_id)
select distinct g.id, f.user_id
from diary_sharing_groups g
join group_members c on c.user_id = g.owner_id and c.member_role = 'CHILD'
join group_members f on f.group_id = c.group_id and f.member_role = 'FAMILY'
join users family on family.id = f.user_id and family.app_role = 'FAMILY'
where g.slot = 0;

update groups set invite_code_revoked = true;
